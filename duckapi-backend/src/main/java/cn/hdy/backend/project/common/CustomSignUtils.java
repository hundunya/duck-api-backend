package cn.hdy.backend.project.common;

import cn.hutool.core.lang.ObjectId;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;

import java.util.HashMap;
import java.util.Map;

import static cn.hdy.backend.project.constant.CommonConstant.SIGN_SALT;

/**
 * 自定义签名工具
 * @author 混沌鸭
 **/
public class CustomSignUtils {
    private CustomSignUtils(){}

    /**
     * 生成签名
     * @return 签名
     */
    public static Map<String, String> generateSign(){
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        HMac hmacMd5 = SecureUtil.hmacMd5();
        String uuid = IdUtil.fastSimpleUUID();
        // 盐值+账号+当前时间戳
        String accessKey = hmacMd5.digestHex(SIGN_SALT+uuid+currentTimeMillis);
        uuid = ObjectId.next();
        String s = accessKey.substring(0, Math.min(accessKey.length(), 5));
        HMac hmacSha256 = SecureUtil.hmacSha256();
        // UUID+当前时间+accessKey前5位
        String secretKey = hmacSha256.digestHex(uuid + currentTimeMillis + s);
        Map<String, String> result = new HashMap<>();
        result.put("accessKey", accessKey);
        result.put("secretKey", secretKey);
        return result;
    }
}
