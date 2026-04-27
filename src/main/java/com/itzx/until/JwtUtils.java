package com.itzx.until;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Date;

@Component
public class JwtUtils {
    // 密钥（实际项目中需配置在application.yml，且长度≥256位）
    @Value("${jwt.secret}")
    private String secret;

    // Token过期时间（如2小时，单位：毫秒）
    @Value("${jwt.expire}")
    private Long expire;

    // 生成Token（基于用户名）- 兼容老方法，默认角色为USER
    public String generateToken(String username) {
        return generateToken(username, "USER");
    }

    // 生成Token（用户名 + 角色）
    public String generateToken(String username, String role) {
        // 设置过期时间（当前时间+过期时长）
        Date expireDate = new Date(System.currentTimeMillis() + expire);

        return Jwts.builder()
                .setSubject(username) // 主题（存储用户名）
                .claim("role", role) // 自定义角色
                .setExpiration(expireDate) // 过期时间
                .signWith(SignatureAlgorithm.HS256, secret) // 签名算法+密钥
                .compact();
    }

    // 从Token中获取用户名
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 从Token中获取角色
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object role = claims.get("role");
        return role == null ? null : role.toString();
    }

    // 验证Token合法性（是否过期、签名是否正确）
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token); // 解析失败会抛出异常
            return true;
        } catch (Exception e) {
            // 异常包括：过期（ExpiredJwtException）、签名错误（SignatureException）等
            return false;
        }
    }
}
