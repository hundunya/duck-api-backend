package cn.hdy.sdk.client.project.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 混沌鸭
 *
 * 解析工具
 **/
public class ApiUtils {

    private static final Gson GSON = new Gson();

    @SuppressWarnings("rawtypes")
    public static Map<String, String> parseHeader(String header){
        try {
            Map<String, String> result = new HashMap<>();
            List list = GSON.fromJson(header, List.class);
            if (list == null || list.isEmpty()){
                // 请求头为空
                return result;
            }
            for (Object object : list) {
                Map map = (Map) object;
                Object name = map.get("name");
                Object content = map.get("content");
                result.put((String) name, (String) content);
            }
            return result;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static List<Map<String, Object>> parseParam(String param){
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            List list = GSON.fromJson(param, List.class);
            if (list == null || list.isEmpty()){
                // 请求头为空参数
                return result;
            }
            for (Object object : list) {
                Map map = (Map) object;
                Map<String, Object> element = new HashMap<>();
                for (Object key : map.keySet()) {
                    Object content = map.get(key);
                    element.put((String) key, content);
                }
                result.add(element);
            }
            return result;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Map<String, String> parseInputParam(String param){
        try {
            Map<String, String> result = new HashMap<>();
            Map map = GSON.fromJson(param, Map.class);
            if (map == null || map.isEmpty()){
                // 请求头为空
                return result;
            }
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                result.put((String) key, (String) value);
            }
            return result;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检验输入的参数是否有效
     * @param type 参数类型
     * @param value 参数值
     * @return 是否有效
     */
    public static boolean validValue(String type, String value){
        if (value == null){
            return true;
        }
        // 参数不为空，校验输入参数
        try {
            switch (type){
                case "byte":
                    Byte.valueOf(value);
                    break;
                case "short":
                    Short.valueOf(value);
                    break;
                case "int":
                    Integer.valueOf(value);
                    break;
                case "long":
                    Long.valueOf(value);
                    break;
                case "float":
                    Float.valueOf(value);
                    break;
                case "double":
                    Double.valueOf(value);
                    break;
                case "boolean":
                    //noinspection ResultOfMethodCallIgnored
                    Boolean.valueOf(value);
                    break;
                case "object":
                    GSON.fromJson(value, Object.class);
                    break;
                default:
                    // 默认为string
                    break;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据方法名获取HttpRequest
     * @param url 请求地址
     * @param method 请求方法名称
     * @return Method
     */
    public static HttpRequest getRequestByMethod(String url, String method){
        switch (method){
            case "PUT":
                return HttpRequest.put(url);
            case "DELETE":
                return HttpRequest.delete(url);
            case "PATCH":
                return HttpRequest.patch(url);
            case "POST":
                return HttpRequest.post(url);
            default:
                return HttpRequest.get(url);
        }
    }

    /**
     * 根据输入type获取随机值
     * @param type 数据类型
     * @return 对应type的随机数据
     */
    public static Object getValueByType(String type){
        switch (type){
            case "byte":
                return (byte) RandomUtil.randomInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
            case "short":
                return (short) RandomUtil.randomInt(Short.MIN_VALUE, Short.MAX_VALUE);
            case "int":
                return RandomUtil.randomInt();
            case "long":
                return RandomUtil.randomLong();
            case "float":
                return RandomUtil.randomFloat();
            case "double":
                return RandomUtil.randomDouble();
            case "boolean":
                return RandomUtil.randomBoolean();
            case "object":
                return "{}";
            default:
                return RandomUtil.randomString(5);
        }
    }
}
