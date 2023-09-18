package cn.hdy.backend.project.aop;

import cn.hdy.backend.project.annotation.LoginToken;
import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.model.enums.UserRoleEnum;
import cn.hdy.backend.project.service.UserService;
import cn.hdy.common.project.model.entity.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 登录权限校验 AOP
 *
 * @author 滴滴鸭
 */
@Aspect
@Component
public class JwtInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param loginToken
     * @return
     */
    @Around("@annotation(loginToken)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, LoginToken loginToken) throws Throwable {
        boolean required = loginToken.required();
        // 必须有该权限才通过
        if (required) {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            // 当前登录用户
            User loginUser = userService.getLoginUser(request);
            // 获取登录用户的权限
            String userRole = loginUser.getUserRole();
            // 根据登录用户权限获取对应的权限枚举值
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(userRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

