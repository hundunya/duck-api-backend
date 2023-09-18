package cn.hdy.backend.project.service;

import cn.hdy.backend.project.model.dto.user.UserQueryRequest;
import cn.hdy.backend.project.model.dto.user.UserUpdatePasswordRequest;
import cn.hdy.common.project.model.entity.User;
import cn.hdy.backend.project.model.vo.LoginUserVO;
import cn.hdy.backend.project.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户服务
 *
 * @author 滴滴鸭
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request 前端请求
     * @param response 响应
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request 前端请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request 前端请求
     * @return 当前登录用户
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request 前端请求
     * @return 用户是否唯恐管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user 用户信息
     * @return 用户是否为管理员
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request 前端请求
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return 当前登录用户视图
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 脱敏前的用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList 用户列表
     * @return 脱敏后的用户列表视图
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 封装的对用户的查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 修改密码
     *
     * @param userUpdatePasswordRequest 用户修改密码请求
     * @param loginUser 当前登录用户
     * @return 修改密码是否成功
     */
    boolean updateMyPassword(UserUpdatePasswordRequest userUpdatePasswordRequest, User loginUser);

    /**
     * 获取用户accessKey
     *
     * @param id 用户ID
     * @return accessKey
     */
    String getAccessKeyById(Long id);

    /**
     * 获取用户secretKey
     *
     * @param id 用户ID
     * @return secretKey
     */
    String getSecretKeyById(Long id);

    /**
     * 更换密钥
     * @param loginUser 当前登录用户
     * @return 更换密钥是否成功
     */
    Boolean updateMyKey(User loginUser);
}
