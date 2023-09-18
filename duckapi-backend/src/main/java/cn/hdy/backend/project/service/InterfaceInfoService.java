package cn.hdy.backend.project.service;

import cn.hdy.backend.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import cn.hdy.backend.project.model.vo.InterfaceInfoVO;
import cn.hdy.common.project.model.entity.InterfaceInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 滴滴鸭
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request);

    Page<InterfaceInfoVO> getInterfaceInfoVoPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);
}
