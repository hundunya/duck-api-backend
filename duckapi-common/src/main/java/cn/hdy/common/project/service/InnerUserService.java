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
}
