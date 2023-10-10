package cn.hdy.backend.project.constant;

/**
 * 用户常量
 *
 * @author 滴滴鸭
 */
public interface UserConstant {

    /**
     * 登录过期时间
     */
    Integer LOGIN_EXPIRE_TIME = 24 * 60 * 60;

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    // region 接口相关

    /**
     * 用户对接口的默认剩余调用次数
     */
    int LEFT_NUM = 100;

    // endregion
}
