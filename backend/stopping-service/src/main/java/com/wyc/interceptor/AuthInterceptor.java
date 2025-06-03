package com.wyc.interceptor;

import com.wyc.utils.JwtTokenUtil;
import com.wyc.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RedisCache redisCache;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 不需要认证的路径直接放行
        if (isPublicPath(requestURI)) {
            return true;
        }

        // 获取token
        String token = request.getHeader(tokenHeader);
        if (!StringUtils.hasText(token) || !token.startsWith(tokenPrefix)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 验证token
        token = token.substring(tokenPrefix.length()).trim();
        try {
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            String redisKey = "login_user:" + userId;
            if (redisCache.getCacheObject(redisKey) == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    private boolean isPublicPath(String requestURI) {
        return requestURI.startsWith("/wyc/users/login") ||
                requestURI.startsWith("/wyc/users/register") ||
                requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/swagger-resources") ||
                requestURI.startsWith("/v3/api-docs");
    }
}
