package cn.hdy.backend.project.service.impl;

import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.constant.CommonConstant;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.exception.ThrowUtils;
import cn.hdy.backend.project.mapper.InterfaceInfoMapper;
import cn.hdy.backend.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import cn.hdy.backend.project.model.vo.InterfaceInfoVO;
import cn.hdy.backend.project.service.InterfaceInfoService;
import cn.hdy.backend.project.utils.SqlUtils;
import cn.hdy.common.project.model.entity.InterfaceInfo;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 滴滴鸭
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {

    private final static Gson GSON = new Gson();

    private final static String[] VALUE_TYPE = {"string", "byte", "short", "int", "long", "boolean", "object"};

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        Integer price = interfaceInfo.getPrice();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String requestParam = interfaceInfo.getRequestParam();
        String responseParam = interfaceInfo.getResponseParam();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, description, url, method), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(price == null, ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口描述过长");
        }
        if (StringUtils.isNotBlank(url) && url.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口地址过长");
        }
        if (price <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口调用单价必须大于0");
        }
        validateHeader(requestHeader);
        validateHeader(responseHeader);
        validateParam(requestParam, true);
        validateParam(responseParam, false);
    }

    @SuppressWarnings("rawtypes")
    private void validateHeader(String header) {
        try {
            if (StrUtil.isNotBlank(header)) {
                List list = GSON.fromJson(header, List.class);
                for (Object object : list) {
                    Map map = (Map) object;
                    String name = (String) map.get("name");
                    String content = (String) map.get("content");
                    if (StrUtil.isBlank(name) || StrUtil.isBlank(content)){
                        throw new RuntimeException();
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    private void validateParam(String param, boolean request) {
        try {
            if (StrUtil.isNotBlank(param)) {
                List list = GSON.fromJson(param, List.class);
                for (Object object : list) {
                    Map map = (Map) object;
                    String name = (String) map.get("name");
                    String type = (String) map.get("type");
                    if (request){
                        boolean require = (boolean) map.get("require");
                    }
                    String description = (String) map.get("description");
                    ThrowUtils.throwIf(
                            StrUtil.isBlank(name) || StrUtil.isBlank(type) || StrUtil.isBlank(description),
                            ErrorCode.PARAMS_ERROR
                    );
                    if (!Arrays.asList(VALUE_TYPE).contains(type)) {
                        // 参数类型不符合要求
                        throw new RuntimeException();
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 获取查询包装类
     *
     * @param interfaceInfoQueryRequest 接口信息查询实体
     * @return 封装后的查询条件
     */
    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = interfaceInfoQueryRequest.getId();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        String url = interfaceInfoQueryRequest.getUrl();
        String method = interfaceInfoQueryRequest.getMethod();
        String requestHeader = interfaceInfoQueryRequest.getRequestHeader();
        String responseHeader = interfaceInfoQueryRequest.getResponseHeader();
        Integer status = interfaceInfoQueryRequest.getStatus();
        Long createUser = interfaceInfoQueryRequest.getCreateUser();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.like(StringUtils.isNotBlank(url), "url", url);
        queryWrapper.eq(StringUtils.isNotBlank(method), "method", method);
        queryWrapper.like(StringUtils.isNotBlank(requestHeader), "request_header", requestHeader);
        queryWrapper.like(StringUtils.isNotBlank(responseHeader), "response_header", responseHeader);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(createUser), "create_user", createUser);
        queryWrapper.eq("is_delete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request) {
        return InterfaceInfoVO.objToVo(interfaceInfo);
    }

    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVoPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVoPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());
        if (CollectionUtils.isEmpty(interfaceInfoList)) {
            return interfaceInfoVoPage;
        }
        // 填充信息
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(InterfaceInfoVO::objToVo).collect(Collectors.toList());
        interfaceInfoVoPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVoPage;
    }
}




