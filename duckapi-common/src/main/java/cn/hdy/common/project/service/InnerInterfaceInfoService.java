package cn.hdy.common.project.service;

import cn.hdy.common.project.model.entity.InterfaceInfo;

/**
 * @author 混沌鸭
 **/
public interface InnerInterfaceInfoService {
    /**
     * 获取接口信息
     * @param path 接口url
     * @param method 接口请求方法
     * @return 接口信息
     */
    InterfaceInfo getInterfaceInfo(String path, String method);

    /**
     * 接口调用计数
     * @param interfaceId 接口ID
     */
    void invokeCount(long interfaceId);
}
