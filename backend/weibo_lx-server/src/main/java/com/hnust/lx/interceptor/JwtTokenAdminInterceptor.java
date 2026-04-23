package com.hnust.lx.interceptor;

import com.hnust.lx.constant.JwtClaimsConstant;
import com.hnust.lx.context.BaseContext;
import com.hnust.lx.properties.JwtProperties;
import com.hnust.lx.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getAdminTokenName());

        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            Integer userType = Integer.valueOf(claims.get(JwtClaimsConstant.USER_TYPE).toString());
            
            if (userType != 1) {
                log.info("非管理员访问: userType={}", userType);
                response.setStatus(403);
                return false;
            }
            
            BaseContext.setCurrentId(userId);
            log.info("当前管理员id：{}", userId);
            return true;
        } catch (Exception ex) {
            log.error("jwt校验失败: {}", ex.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\",\"data\":null}");
            return false;
        }
    }
}
