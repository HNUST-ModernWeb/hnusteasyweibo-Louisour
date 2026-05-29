// config/LoginInterceptor.java
package com.weibo.config;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // 放行静态资源和登录相关路径
        if (uri.startsWith("/css/") || uri.startsWith("/js/") ||
            uri.startsWith("/uploads/") || uri.startsWith("/login") ||
            uri.equals("/doLogin") || uri.equals("/logout")) {
            return true;
        }
        
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            return true; // 已登录
        }
        
        // 对于API请求返回401状态码
        if (uri.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未登录，请先登录\"}");
            return false;
        }
        
        // 其他页面请求重定向到登录页
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}