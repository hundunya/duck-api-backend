package cn.hdy.gateway.project.exception;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * 全局异常处理器
 *
 * @author 滴滴鸭
 */
@Slf4j
@Order(-2)
@Component
public class GlobalExceptionHandler implements WebExceptionHandler {

    @NotNull
    @Override
    public Mono<Void> handle(ServerWebExchange exchange,@NotNull Throwable ex) {
        ex.printStackTrace();
        log.info("异常响应日志：{}", ex.getMessage());
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        // 设置返回JSON
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatus());
        }
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            String message = ex.getMessage();
            if (message == null || message.length() > 50){
                message = "系统错误";
            }
            //返回响应结果
            return bufferFactory.wrap(message.getBytes());
        }));
    }
}
