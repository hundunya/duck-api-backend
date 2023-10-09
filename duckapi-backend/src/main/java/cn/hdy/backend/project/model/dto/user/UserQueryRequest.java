package cn.hdy.backend.project.model.dto.user;

import cn.hdy.backend.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author 滴滴鸭
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

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