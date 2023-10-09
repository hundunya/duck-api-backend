package cn.hdy.gateway.project.utils;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * @author 混沌鸭
 * 签名工具类
 **/
public class SignUtils {
    /**
     * 生成签名
     * @param body 用户参数
     * @param secretKey 密钥
     * @return 生成的签名
     */
    public static String genSign(String body, String secretKey){
        return DigestUtil.md5Hex(body+secretKey);
    }
}
