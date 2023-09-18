package cn.hdy.common.project.service;

/**
* @author 滴滴鸭
*/
public interface InnerUserInterfaceService {
    /**
     * 接口调用计数
     * @param interfaceId 接口ID
     * @param userId 用户ID
     */
    void invokeCount(long interfaceId, long userId) throws Exception;
}
