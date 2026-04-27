package com.itzx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itzx.until.JwtUtils;
import com.itzx.until.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的Token（格式通常为：Authorization: Bearer token）
        String token = request.getHeader("Authorization");
        // 2. 判断Token是否存在
        if (token == null || !token.startsWith("Bearer ")) {
            // 无Token或格式错误，返回401
            response.setContentType("application/json,charset=UTF-8");
            //将序列化后的 JSON 字符串（由 ObjectMapper.writeValueAsString 生成）写入 HTTP 响应体
            response.getWriter().write(new ObjectMapper().writeValueAsString(Result.unAuth("请先登录")));
            return false;
        }

        // 3. 去除"Bearer "前缀，获取真实Token
        token = token.substring(7);

        // 4. 验证Token
        if (!jwtUtils.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(Result.unAuth("Token无效或已过期")));
            return false;
        }

        // 5. Token有效，放行（可将用户名和角色存入request，供后续接口使用）
        String username = jwtUtils.getUsernameFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        return true;
    }
}
