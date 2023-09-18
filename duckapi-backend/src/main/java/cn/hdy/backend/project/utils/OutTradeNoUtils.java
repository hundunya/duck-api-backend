package cn.hdy.backend.project.utils;

/**
 * @author 混沌鸭
 *
 * 订单号生成工具类
 **/
public class OutTradeNoUtils {

    public static String getOutTradeNo() {
        long currentTimeMillis = System.currentTimeMillis();
        return String.valueOf(currentTimeMillis);
    };
}
