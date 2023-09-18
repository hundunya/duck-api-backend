package cn.hdy.backend.project.utils;

import cn.hdy.common.project.model.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

/**
 * @author 滴滴鸭
 */
public class TokenUtils {
    /**
     * 过期时间5分钟
     */
    private static final long EXPIRE_TIME = 5 * 60 * 1000;

    public static String getToken(User user) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        // 将 user id 保存到 token 里面
        return JWT.create().withAudience(String.valueOf(user.getId()))
                //五分钟后token过期
                .withExpiresAt(date)
                // 以 password 作为 token 的密钥
                .sign(Algorithm.HMAC256(user.getUserPassword()));
    }
}