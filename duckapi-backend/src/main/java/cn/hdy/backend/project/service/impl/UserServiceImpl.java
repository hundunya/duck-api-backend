package cn.hdy.backend.project.service.impl;

import cn.hdy.backend.project.common.CustomSignUtils;
import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.constant.CommonConstant;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.mapper.UserMapper;
import cn.hdy.backend.project.model.dto.user.UserQueryRequest;
import cn.hdy.backend.project.model.dto.user.UserUpdatePasswordRequest;
import cn.hdy.backend.project.model.enums.UserRoleEnum;
import cn.hdy.backend.project.model.vo.LoginUserVO;
import cn.hdy.backend.project.model.vo.UserVO;
import cn.hdy.backend.project.service.UserService;
import cn.hdy.backend.project.utils.SqlUtils;
import cn.hdy.backend.project.utils.TokenUtils;
import cn.hdy.common.project.model.entity.User;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author 滴滴鸭
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "DidiDuck";

    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过长");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (userPassword.length() > 20 || checkPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过长");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_account", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName("user_" + userPassword);
            // 4. 分配签名
            Map<String, String> sign = CustomSignUtils.generateSign();
            String accessKey = sign.get("accessKey");
            String secretKey = sign.get("secretKey");
            if (StrUtil.isBlank(accessKey) || StrUtil.isBlank(secretKey)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
            }
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3.生成token(为了方便前端，因此此处是将token设置为cookie)
        String token = TokenUtils.getToken(user);
        response.setHeader("token", token);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(token, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        String token = request.getHeader("token");
        Object userObj = request.getSession().getAttribute(token);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        String token = request.getHeader("token");
        Object userObj = request.getSession().getAttribute(token);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        String token = request.getHeader("token");
        Object userObj = request.getSession().getAttribute(token);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (request.getSession().getAttribute(token) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(token);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "union_id", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mp_open_id", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "user_profile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "user_name", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean updateMyPassword(UserUpdatePasswordRequest userUpdatePasswordRequest, User loginUser) {
        // 1.输入参数校验
        String oldUserPassword = userUpdatePasswordRequest.getOldUserPassword();
        String newUserPassword = userUpdatePasswordRequest.getNewUserPassword();
        String checkUserPassword = userUpdatePasswordRequest.getCheckUserPassword();
        if (StrUtil.isBlank(oldUserPassword) || StrUtil.isBlank(newUserPassword) || StrUtil.isBlank(checkUserPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入不能为空");
        }
        if (oldUserPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过长");
        }
        if (newUserPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过长");
        }
        if (checkUserPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过长");
        }
        if (!newUserPassword.equals(checkUserPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不相等");
        }
        // 2.旧密码加密
        String oldEncryptPassword = DigestUtils.md5DigestAsHex((SALT + oldUserPassword).getBytes());
        synchronized (loginUser.getUserAccount().intern()) {
            // 3.数据库校验旧密码
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", loginUser.getId());
            queryWrapper.eq("user_password", oldEncryptPassword);
            User user = userMapper.selectOne(queryWrapper);
            // 用户为空
            if (user == null) {
                log.info("user modify password failed, oldUserPassword cannot match userPassword");
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码输入错误");
            }
            // 5.判断新旧密码是否相同
            String newEncryptPassword = DigestUtils.md5DigestAsHex((SALT + newUserPassword).getBytes());
            if (newEncryptPassword.equals(oldEncryptPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与旧密码相同");
            }
            // 4.更新密码
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", user.getId());
            updateWrapper.set("user_password", newEncryptPassword);
            boolean success = this.update(updateWrapper);
            if (!success) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改密码失败，请重试");
            }
            return true;
        }
    }

    @Override
    public String getAccessKeyById(Long id) {
        return userMapper.selectAccessKeyById(id);
    }

    @Override
    public String getSecretKeyById(Long id) {
        return userMapper.selectSecretKeyById(id);
    }

    @Override
    public Boolean updateMyKey(User loginUser) {
        synchronized (loginUser.getUserAccount().intern()) {
            User user = new User();
            Map<String, String> sign = CustomSignUtils.generateSign();
            String accessKey = sign.get("accessKey");
            String secretKey = sign.get("secretKey");
            if (StrUtil.isBlank(accessKey) || StrUtil.isBlank(secretKey)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更换密钥失败");
            }
            user.setId(loginUser.getId());
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            return this.updateById(user);
        }
    }
}
