package cn.hdy.backend.project.controller;

import cn.hdy.backend.project.annotation.AuthCheck;
import cn.hdy.backend.project.common.*;
import cn.hdy.backend.project.constant.UserConstant;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.exception.ThrowUtils;
import cn.hdy.backend.project.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import cn.hdy.backend.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import cn.hdy.backend.project.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import cn.hdy.backend.project.model.dto.interfaceinfo.InvokeInterfaceParamRequest;
import cn.hdy.backend.project.model.enums.InterfaceStatusEnum;
import cn.hdy.backend.project.model.vo.InterfaceInfoVO;
import cn.hdy.backend.project.service.InterfaceInfoService;
import cn.hdy.backend.project.service.UserInterfaceService;
import cn.hdy.backend.project.service.UserService;
import cn.hdy.common.project.model.entity.InterfaceInfo;
import cn.hdy.common.project.model.entity.User;
import cn.hdy.common.project.model.entity.UserInterface;
import cn.hdy.sdk.client.project.client.ApiClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static cn.hdy.backend.project.constant.UserConstant.LEFT_NUM;

/**
 * API信息接口
 *
 * @author 滴滴鸭
 */
@RestController
@RequestMapping("/interface-info")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private UserInterfaceService userInterfaceService;

    @Resource
    private ApiClient apiClient;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest 接口信息添加实体
     * @return 添加的接口信息ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setCreateUser(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest 删除实体
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 批量删除
     *
     * @param ids id数组
     * @return 删除是否成功
     */
    @PostMapping("/batch/delete")
    public BaseResponse<Boolean> deleteInterfaceInfoByIds(@RequestBody List<Long> ids, HttpServletRequest request) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        for (Long id : ids) {
            ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR);
            // 判断是否存在
            InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
            ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeBatchByIds(ids);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest 接口信息更新实体
     * @return 更新是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id 接口信息ID
     * @return 接口信息VO
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVoById(long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询接口信息
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        InterfaceInfoVO interfaceInfoVO = interfaceInfoService.getInterfaceInfoVO(interfaceInfo, request);
        // 给接口设置默认剩余调用次数
        interfaceInfoVO.setLeftNum(LEFT_NUM);
        // 查询接口剩余调用次数
        QueryWrapper<UserInterface> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        queryWrapper.eq("interface_id", interfaceInfo.getId());
        UserInterface userInterface = userInterfaceService.getOne(queryWrapper);
        if (userInterface != null) {
            interfaceInfoVO.setLeftNum(userInterface.getLeftNum());
        }
        return ResultUtils.success(interfaceInfoVO);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest 接口信息查询实体
     * @return 分页查询结果
     */
    @SuppressWarnings("uncheck")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVoByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVoPage(interfaceInfoPage, request));
    }

    // endregion

    /**
     * 发布接口（仅管理员）
     *
     * @param idRequest id实体
     * @return 发布是否成功
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterface(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(idRequest, interfaceInfo);
        long id = idRequest.getId();
        // 判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 判断接口是否可以调用
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();
        String requestHeader = oldInterfaceInfo.getRequestHeader();
        String requestParam = oldInterfaceInfo.getRequestParam();

        boolean success = apiClient.execute(url, method, requestHeader, requestParam);
        ThrowUtils.throwIf(!success, ErrorCode.PARAMS_ERROR, "接口无法访问，发布失败");
        // 发布接口
        interfaceInfo.setStatus(InterfaceStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 下线接口（仅管理员）
     *
     * @param idRequest id实体
     * @return 下线是否成功
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterface(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(idRequest, interfaceInfo);
        long id = idRequest.getId();
        // 判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 下线接口
        interfaceInfo.setStatus(InterfaceStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 接口调试
     *
     * @param invokeInterfaceParamRequest id实体
     * @return 调试结果
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterface(@RequestBody InvokeInterfaceParamRequest invokeInterfaceParamRequest, HttpServletRequest request) {
        if (invokeInterfaceParamRequest == null || invokeInterfaceParamRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = invokeInterfaceParamRequest.getId();
        // 1.判断接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 接口调试
        ApiClient client = new ApiClient();
        client.setAccessKey(loginUser.getAccessKey());
        client.setSecretKey(loginUser.getSecretKey());
        // 2.获取接口所需参数
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        String requestHeader = interfaceInfo.getRequestHeader();
        String requestParam = interfaceInfo.getRequestParam();
        String param = invokeInterfaceParamRequest.getParam();
        // 3.调用接口
        String result = client.execute(url, method, param, requestHeader, requestParam);
        return ResultUtils.success(result);
    }

    /**
     * 获取接口剩余调用次数
     *
     * @param interfaceId 接口ID
     * @return 剩余调用次数
     */
    @GetMapping("/get/interface/leftNum")
    public BaseResponse<Integer> getInterfaceLeftNum(long interfaceId, HttpServletRequest request) {
        if (interfaceId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceId);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //获取剩余调用次数
        QueryWrapper<UserInterface> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        queryWrapper.eq("interface_id", interfaceInfo.getId());
        UserInterface userInterface = userInterfaceService.getOne(queryWrapper);
        Integer leftNum = LEFT_NUM;
        if (userInterface != null) {
            leftNum = userInterface.getLeftNum();
        }
        return ResultUtils.success(leftNum);
    }


}
