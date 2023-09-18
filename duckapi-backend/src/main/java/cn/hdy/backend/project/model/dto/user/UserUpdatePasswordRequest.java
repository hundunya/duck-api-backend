package cn.hdy.backend.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新密码请求
 *
 * @author 滴滴鸭
 */
@Data
public class UserUpdatePasswordRequest implements Serializable {

    /**
     * 旧密码
     */
    private String oldUserPassword;

    /**
     * 新密码
     */
    private String newUserPassword;

    /**
     * 确认密码
     */
    private String checkUserPassword;

    private static final long serialVersionUID = 1L;
}