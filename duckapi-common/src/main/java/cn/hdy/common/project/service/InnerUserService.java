package cn.hdy.common.project.service;

import cn.hdy.common.project.model.entity.User;

/**
 * @author 混沌鸭
 **/
public interface InnerUserService {
    /**
     * 根据accessKey获取调用用户信息
     * @param accessKey 用户密钥账号
     * @return 用户信息
     */
    User getInvokeUser(String accessKey);

    /**
     * 更新调用接口的用户金币余额
     * @param invokeUser 调用者信息
     * @return 更新是否成功
     */
    boolean updateGoldCoinBalance(User invokeUser);
}
