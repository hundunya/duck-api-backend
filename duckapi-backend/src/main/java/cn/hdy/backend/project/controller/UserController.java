package cn.hdy.backend.project.controller;

import cn.hdy.backend.project.annotation.AuthCheck;
import cn.hdy.backend.project.common.BaseResponse;
import cn.hdy.backend.project.common.DeleteRequest;
import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.common.ResultUtils;
import cn.hdy.backend.project.constant.UserConstant;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.exception.ThrowUtils;
import cn.hdy.backend.project.model.dto.user.*;
import cn.hdy.backend.project.utils.ImageUtils;
import cn.hdy.common.project.model.entity.User;
import cn.hdy.backend.project.model.vo.LoginUserVO;
import cn.hdy.backend.project.model.vo.UserVO;
import cn.hdy.backend.project.service.UserService;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户接口
 *
 * @author 滴滴鸭
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private ImageUtils imageUtils;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册信息
     * @return 注册成功ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录账户
     * @param request 请求
     * @return 用户信息和令牌
     */
    @PostMapping("/login")
    public BaseResponse<Map<String, Object>> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request, response);
        String token = response.getHeader("token");
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", loginUserVO);
        return ResultUtils.success(map);
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 注销是否成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest 用户信息
     * @return 添加成功的用户ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 用户ID
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest 待用户信息
     * @return 更新是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVoById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest 用户查询条件
     * @return 用户列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest 用户查询条件
     * @return 用户列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVoPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVoPage.setRecords(userVO);
        return ResultUtils.success(userVoPage);
    }

    // endregion

    // region 个人信息相关
    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 用户更新后的信息
     * @param request 请求
     * @return 更新是否成功
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 修改密码
     *
     * @param userUpdatePasswordRequest 用户更新后的密码
     * @param request 请求
     * @return 修改密码是否成功
     */
    @PostMapping("/update/my/password")
    public BaseResponse<Boolean> updateMyPassword(@RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest,
                                              HttpServletRequest request) {
        if (userUpdatePasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = userService.updateMyPassword(userUpdatePasswordRequest, loginUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更换头像
     *
     * @param multipartFile 用户更新后的头像
     * @param request 请求
     * @return 更新是否成功
     */
    @PostMapping("/update/my/avatar")
    public BaseResponse<Boolean> updateMyAvatar(MultipartFile multipartFile,
                                                  HttpServletRequest request) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 1.上传图片到服务器
        File file = null;
        String url;
        try {
            // 获取文件的后缀
            String contentType = multipartFile.getContentType();
            ThrowUtils.throwIf(contentType == null, ErrorCode.SYSTEM_ERROR);
            String suffix = "." + contentType.substring(contentType.lastIndexOf("/") + 1);
            // 生成文件
            file = new File(IdUtil.simpleUUID() + suffix);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(multipartFile.getBytes());
            outputStream.close();
            // 上传文件
            url = imageUtils.upLoad("duckapi/", file);
            if ("".equals(url)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.info("删除文件 {} 失败", file.getName());
                }
            }
        }
        // 2.更新数据库用户头像
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(url);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    // region 密钥相关

    /**
     * 获取accessKey
     *
     * @param request 请求
     * @return accessKey
     */
    @PostMapping("/get/my/key/access")
    public BaseResponse<String> getAccessKey(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String accessKey = userService.getAccessKeyById(loginUser.getId());
        ThrowUtils.throwIf(StrUtil.isBlank(accessKey), ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(accessKey);
    }

    /**
     * 获取secretKey
     *
     * @param request 请求
     * @return secretKey
     */
    @PostMapping("/get/my/key/secret")
    public BaseResponse<String> getSecretKey(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String secretKey = userService.getSecretKeyById(loginUser.getId());
        ThrowUtils.throwIf(StrUtil.isBlank(secretKey), ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(secretKey);
    }

    /**
     * 更换密钥对
     *
     * @param request 请求
     * @return 更换密钥是否成功
     */
    @PostMapping("/update/my/key")
    public BaseResponse<Boolean> updateMyKey(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Boolean result = userService.updateMyKey(loginUser);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
