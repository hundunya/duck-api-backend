package cn.hdy.backend.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author 滴滴鸭
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 签名
     */
    private String signature;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 金币余额
     */
    private Integer goldCoinBalance;

    private static final long serialVersionUID = 1L;
}