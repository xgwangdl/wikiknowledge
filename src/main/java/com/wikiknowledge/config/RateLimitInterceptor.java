package com.wikiknowledge.config;

import com.wikiknowledge.common.RateLimitException;
import com.wikiknowledge.common.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;/** 全局限流拦截器 */


@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    /**
     * 请求进入 Controller 前执行限流校验，命中限流时直接返回 429。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 目标处理器
     * @return true 表示放行，false 表示已返回限流响应
     * @throws Exception 拦截器异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String username = request.getUserPrincipal() == null
                ? "anonymous"
                : request.getUserPrincipal().getName();
        try {
            rateLimitService.check(username, request.getRemoteAddr());
            return true;
        } catch (RateLimitException ex) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"status\":429,\"code\":\"RATE_LIMITED\",\"message\":\""
                    + ex.getMessage() + "\"}");
            return false;
        }
    }
}
