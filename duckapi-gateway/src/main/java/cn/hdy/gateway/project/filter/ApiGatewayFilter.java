package cn.hdy.gateway.project.filter;

import cn.hdy.common.project.model.entity.InterfaceInfo;
import cn.hdy.common.project.model.entity.User;
import cn.hdy.common.project.service.InnerInterfaceInfoService;
import cn.hdy.common.project.service.InnerUserInterfaceService;
import cn.hdy.common.project.service.InnerUserService;
import cn.hdy.gateway.project.common.ResultUtils;
import cn.hdy.gateway.project.utils.NetUtils;
import cn.hdy.gateway.project.utils.SignUtils;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 混沌鸭
 **/
@Slf4j
@Component
public class ApiGatewayFilter implements GatewayFilter {

    /**
     * IP白名单
     */
    private final String[] WHITE_LIST = {"127.0.0.1", "192.168.3.28"};
    /**
     * 5 分钟
     */
    private final Long FIVE_MINUTES = 5 * 60L;

    @DubboReference
    private InnerInterfaceInfoService interfaceInfoService;

    @DubboReference
    private InnerUserInterfaceService userInterfaceService;

    @DubboReference
    private InnerUserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 1. 获取请求路径
        URI uri = request.getURI();
        RequestPath path = request.getPath();
        HttpMethod method = request.getMethod();
        log.info("==============================");
        log.info("请求url：{}", uri.toString() + path);
        log.info("请求路径：{}", path);
        log.info("请求方法：{}", method);
        log.info("==============================");
        // 2. 获取请求参数
        HttpHeaders headers = request.getHeaders();
        String online = headers.getFirst("online");
        log.info("==============================");
        log.info("请求参数：{}", headers);
        log.info("上线接口: {}", online);
        // 3. 记录请求日志
        log.info("请求日志记录");
        log.info("==============================");
        // 4. 判断黑白名单（防止DDOS攻击）
        String ip = NetUtils.getIpAddress(request);
        if (StrUtil.isBlank(ip)){
            // IP为空
            log.info("==============================");
            log.info("访问IP为空");
            log.info("==============================");
            return handleNoAuth(response);
        }
        if (Arrays.stream(WHITE_LIST).noneMatch(s -> s.equals(ip))){
            log.info("==============================");
            log.info("非白名单IP访问：{}", ip);
            log.info("==============================");
            return handleNoAuth(response);
        }
        // 5. 权限校验（ak、sk是否正确）
        String accessKey = headers.getFirst("accessKey");
        User invokeUser = userService.getInvokeUser(accessKey);
        if ("true".equals(online)){
            // 本次操作为上线接口
            String userRole = invokeUser.getUserRole();
            if (!"admin".equals(userRole)){
                return handleNoAuth(response);
            }
        }else {
            // 本次操作为调用接口
            if (!hasAuth(headers, invokeUser)){
                log.info("==============================");
                log.info("用户权限不足");
                log.info("==============================");
                return handleNoAuth(response);
            }
        }
        // 6. 判断请求接口是否存在
        if (method == null){
            log.info("==============================");
            log.info("请求接口不存在！");
            log.info("==============================");
            return handleNoAuth(response);
        }
        String value = path.value();
        value = value.substring(4);
        InterfaceInfo interfaceInfo = interfaceInfoService.getInterfaceInfo(value, method.name());
        if (interfaceInfo == null){
            log.info("==============================");
            log.info("接口不存在！");
            log.info("==============================");
            return handleNoAuth(response);
        }
        // 7. 转发路由，调用接口
        try {
            HttpStatus statusCode = response.getStatusCode();
            if(statusCode == HttpStatus.OK){
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {

                    @NotNull
                    @Override
                    public Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            DataBufferFactory bufferFactory = response.bufferFactory();
                            // 判断响应状态
                            HttpStatus statusCode = this.getStatusCode();
                            if (!HttpStatus.OK.equals(statusCode)){
                                return handleInvokeErrorResponse(statusCode);
                            }
                            StringBuilder sb = new StringBuilder();
                            if (!"true".equals(online)){
                                // 不是用户调用接口
                                // 8. 调用成功，则调用次数+1
                                try {
                                    userInterfaceService.invokeCount(interfaceInfo.getId(), invokeUser.getId());
                                    log.info("==============================");
                                    log.info("接口调用计数成功");
                                    log.info("==============================");
                                } catch (Exception e) {
                                    log.info("==============================");
                                    log.info("接口调用计数失败");
                                    log.info("异常描述: {}", e.getMessage());
                                    log.info("==============================");
                                    // 业务出现异常（包括：剩余调用次数为0、系统异常）
                                    if (e.getMessage().length() >= 50){
                                        return handleErrorResponse("系统错误");
                                    }
                                    return super.writeWith(Mono.fromSupplier(() -> {
                                        // 接口计数失败（包括没有剩余调用次数）
                                        String message = e.getMessage();
                                        log.info("==============================");
                                        log.info("响应日志：{}", message);
                                        log.info("==============================");
                                        //返回响应结果
                                        return bufferFactory.wrap(ResultUtils.error(40000, message));
                                    }));
                                }
                            }
                            // 正常响应
                            fluxBody = fluxBody.map(dataBuffer -> {
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                sb.append(new String(content, StandardCharsets.UTF_8));
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                return bufferFactory.wrap(content);
                            });

                            // 响应完成记录日志
                            fluxBody = fluxBody.doOnComplete(() -> {
                                // 响应数据
                                String data = sb.toString();//data
                                // 10. 记录响应日志
                                log.info("==============================");
                                log.info("响应日志：{}", data);
                                log.info("==============================");
                            });
                            // 9. 返回响应结果
                            return super.writeWith(fluxBody);
                        } else {
                            log.info("==============================");
                            log.error("<--- {} 响应code异常", getStatusCode());
                            log.info("==============================");
                        }
                        return super.writeWith(body);
                    }

                    /**
                     * 处理调用错误响应
                     * @param statusCode 响应状态码
                     * @return 响应视图
                     */
                    private Mono<Void> handleInvokeErrorResponse(HttpStatus statusCode){
                        this.setStatusCode(HttpStatus.OK);
                        if (statusCode == null){
                            return handleErrorResponse("系统错误");
                        }
                        switch (statusCode){
                            // 400: 错误请求
                            case BAD_REQUEST:
                                return handleErrorResponse("参数错误");
                            // 403: 禁止访问
                            case FORBIDDEN:
                                return handleErrorResponse("权限不足");
                            // 404: 接口不存在
                            case NOT_FOUND:
                                return handleErrorResponse("接口不存在");
                            // 408: 请求超时
                            case REQUEST_TIMEOUT:
                                return handleErrorResponse("请求超时");
                            // 429: 访问人数过多
                            case TOO_MANY_REQUESTS:
                                return handleErrorResponse("当前访问人数过多，请稍候再试");
                            // 502: 接口关闭
                            case BAD_GATEWAY:
                                return handleErrorResponse("接口已关闭");
                            // 504: 网关超时
                            case GATEWAY_TIMEOUT:
                                return handleErrorResponse("网关超时");
                            // 其他错误
                            default:
                                return handleErrorResponse("系统错误");
                        }
                    }

                    /**
                     * 处理错误响应
                     * @param message 错误提示
                     * @return 响应视图
                     */
                    private Mono<Void> handleErrorResponse(String message){
                        DataBufferFactory bufferFactory = response.bufferFactory();
                        return super.writeWith(Mono.fromSupplier(() -> {
                            //返回响应结果
                            return bufferFactory.wrap(ResultUtils.error(40000, message));
                        }));
                    }
                };
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        }catch (Exception e){
            log.info("==============================");
            log.error("gateway log exception.\n" + e);
            log.info("==============================");
            throw new RuntimeException(e);
        }
    }

    private Mono<Void> handleNoAuth(ServerHttpResponse response){
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            //返回响应结果
            return bufferFactory.wrap(ResultUtils.error(40000, "权限不足"));
        }));
    }

    /**
     * 权限校验
     * @param httpHeaders 请求头
     * @param invokeUser 调用接口的用户
     * @return 校验结果
     */
    private boolean hasAuth(HttpHeaders httpHeaders, User invokeUser){
        // 1、参数校验
        if (!validHeaders(httpHeaders)){
            return false;
        }
        if (invokeUser == null){
            return false;
        }
        // 2、参数获取
        String accessKey = httpHeaders.getFirst("accessKey");
        String sign = httpHeaders.getFirst("sign");
        String nonce = httpHeaders.getFirst("nonce");
        String timestamp = httpHeaders.getFirst("timestamp");
        String body = httpHeaders.getFirst("body");
        // 3、签名比较
        Map<String, String> headers = new HashMap<>();
        headers.put("accessKey", accessKey);
        headers.put("nonce", nonce);
        headers.put("timestamp", timestamp);
        headers.put("body", body);
        return SignUtils.genSign(headers.toString(), invokeUser.getSecretKey()).equals(sign);
    }

    /**
     * 请求头参数校验
     * @param httpHeaders 请求头
     * @return 校验结果
     */
    private boolean validHeaders(HttpHeaders httpHeaders){
        if (httpHeaders == null){
            return false;
        }
        String accessKey = httpHeaders.getFirst("accessKey");
        String sign = httpHeaders.getFirst("sign");
        String nonce = httpHeaders.getFirst("nonce");
        String timestamp = httpHeaders.getFirst("timestamp");
        if (StringUtils.isBlank(sign)){
            return false;
        }
        if (timestamp == null){
            return false;
        }
        if (!StringUtils.isNumeric(timestamp)){
            return false;
        }
        //判断时间戳是否过期
        long interval = (System.currentTimeMillis() / 1000) - Long.parseLong(timestamp);
        //5分钟过期
        if (interval > FIVE_MINUTES) {
            return false;
        }
        if (StringUtils.isBlank(nonce) || nonce.length()>5){
            return false;
        }
        return !StringUtils.isBlank(accessKey);
    }
}
