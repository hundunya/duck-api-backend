package cn.hdy.backend.project.aop;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @author 混沌鸭
 **/
@Component
public class CustomInterceptor implements HandlerInterceptor {

    private final String[] METHOD_WHITE_LIST = {"OPTIONS"};

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        String origin = request.getHeader("origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "content-type, token");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS");
        String method = request.getMethod();
        if (Arrays.asList(METHOD_WHITE_LIST).contains(method)){
            response.setStatus(200);
            return false;
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
