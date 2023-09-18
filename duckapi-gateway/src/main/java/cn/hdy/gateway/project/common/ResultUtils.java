package cn.hdy.gateway.project.common;

import com.google.gson.Gson;

/**
 * 返回工具类
 *
 * @author 滴滴鸭
 */
public class ResultUtils {

    private static final Gson GSON = new Gson();

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> byte[] success(T data) {
        BaseResponse<T> response = new BaseResponse<>(0, data, "ok");
        return GSON.toJson(response).getBytes();
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static byte[] error(int code, String message) {
        BaseResponse response = new BaseResponse<>(code, null, message);
        return GSON.toJson(response).getBytes();
    }
}
