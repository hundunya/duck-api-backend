package cn.hdy.sdk.client.project.client;

import cn.hdy.sdk.client.project.model.User;
import cn.hdy.sdk.client.project.utils.ApiUtils;
import cn.hdy.sdk.client.project.utils.SignUtils;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 混沌鸭
 **/
@Slf4j
@Data
public class ApiClient {
    private String accessKey;
    private String secretKey;
    private final String BASE_URL = "http://localhost:8200/api";

    private final Gson GSON = new Gson();

    private Map<String, String> getHeaders(String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("accessKey", accessKey);
        String nonce = RandomUtil.randomString(5);
        headers.put("nonce", nonce);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        headers.put("timestamp", timestamp);
        headers.put("body", URLEncodeUtil.encode(body, StandardCharsets.UTF_8));
        String sign = SignUtils.genSign(headers.toString(), secretKey);
        headers.put("sign", sign);
        return headers;
    }

    private Map<String, String> getHeaders(String body, Map<String, String> requestHeaders) {
        Map<String, String> headers = getHeaders(body);
        if (requestHeaders == null) {
            return headers;
        }
        for (String key : requestHeaders.keySet()) {
            String value = requestHeaders.get(key);
            headers.put(key, value);
        }
        return headers;
    }

    public String getNameUsingGet(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        try (HttpResponse response = HttpRequest.get(BASE_URL + "/name/get")
                .form(params)
                .addHeaders(getHeaders(params.toString()))
                .execute()) {
            return response.body();
        }
    }

    public String getNameUsingPost(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        try (HttpResponse response = HttpRequest.post(BASE_URL + "/name/post/name")
                .form(params)
                .addHeaders(getHeaders(params.toString()))
                .execute()) {
            return response.body();
        }
    }

    public String getUsernameUsingPost(User user) {
        String body = JSONUtil.toJsonStr(user);
        try (HttpResponse response = HttpRequest.post(BASE_URL + "/name/post/username")
                .body(body)
                .addHeaders(getHeaders(body))
                .execute()) {
            return response.body();
        }
    }

    /**
     * 执行请求
     *
     * @param url           请求地址
     * @param method        请求方法
     * @param param         请求传入的参数
     * @param requestHeader 请求头
     * @param requestParam  请求参数格式
     * @return 请求结果
     */
    @SuppressWarnings("rawtypes")
    public String execute(String url, String method, String param, String requestHeader, String requestParam) {
        // 1.解析请求头、请求参数以及前端传入的参数
        Map<String, String> paramMap = ApiUtils.parseInputParam(param);
        Map<String, String> requestHeaderMap = ApiUtils.parseHeader(requestHeader);
        List<Map<String, Object>> requestParamList = ApiUtils.parseParam(requestParam);
        // 2.根据请求参数格式校验输入参数是否符合要求
        if (paramMap.size() > requestParamList.size()) {
            return "参数错误";
        }

        for (Map<String, Object> requestParamMap : requestParamList) {
            // 参数名称
            String name = (String) requestParamMap.get("name");
            // 是否必填
            boolean require = (boolean) requestParamMap.get("require");
            // 参数类型
            String type = (String) requestParamMap.get("type");
            String value = paramMap.get(name);
            if (require && value == null) {
                return "参数错误";
            }
            boolean valid = ApiUtils.validValue(type, value);
            if (!valid) {
                return "参数错误";
            }
        }
        // 3.设置请求头
        Map<String, String> headers = getHeaders(param, requestHeaderMap);
        // 4.发送请求
        HttpRequest request = ApiUtils.getRequestByMethod(BASE_URL+url, method);
        request.body(param).addHeaders(headers);
        HttpResponse response;
        try {
            response = request.execute();
        } catch (Exception e) {
            return "接口不存在";
        }
        try (response) {
            String body = response.body();
            log.info("==============================");
            log.info("响应内容: {}", body);
            log.info("==============================");
            try {
                Map map = GSON.fromJson(body, Map.class);
                double code = (double) map.get("code");
                if (code == 40000){
                    return (String) map.get("message");
                }
            } catch (Exception e) {
                // json转换失败
            }
            if (response.isOk()) {
                // 请求成功
                return body;
            }
            if (body.length() > 10){
                return "请求错误";
            }
            return body;
        }
    }

    /**
     * 执行请求，随机设置参数
     *
     * @param url           请求地址
     * @param method        请求方法
     * @param requestHeader 请求头
     * @param requestParam  请求参数格式
     * @return 请求是否成功
     */
    @SuppressWarnings("rawtypes")
    public boolean execute(String url, String method, String requestHeader, String requestParam) {
        // 1.解析请求头、请求参数以及前端传入的参数
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, String> requestHeaderMap = ApiUtils.parseHeader(requestHeader);
        List<Map<String, Object>> requestParamList = ApiUtils.parseParam(requestParam);
        // 2.根据请求参数格式设置输入参数
        for (Map<String, Object> requestParamMap : requestParamList) {
            // 参数名称
            String name = (String) requestParamMap.get("name");
            // 是否必填
            boolean require = (boolean) requestParamMap.get("require");
            // 参数类型
            String type = (String) requestParamMap.get("type");
            if (require){
                Object value = ApiUtils.getValueByType(type);
                paramMap.put(name, value);
            }
        }
        String param = JSONUtil.toJsonStr(paramMap);
        // 3.设置请求头
        Map<String, String> headers = getHeaders(param, requestHeaderMap);
        headers.put("online", "true");
        // 4.发送请求
        HttpRequest request = ApiUtils.getRequestByMethod(BASE_URL+url, method);
        request.body(param).addHeaders(headers);
        HttpResponse response;
        try {
            response = request.execute();
        } catch (Exception e) {
            return false;
        }
        try (response) {
            String body = response.body();
            log.info("==============================");
            log.info("响应内容: {}", body);
            log.info("==============================");
            try {
                Map map = GSON.fromJson(body, Map.class);
                double code = (double) map.get("code");
                if (code == 40000){
                    return false;
                }
            } catch (Exception e) {
                // json转换失败
            }
            return response.isOk();
        }
    }
}
