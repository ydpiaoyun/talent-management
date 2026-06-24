package com.talent.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * <p>
 * 提供 Token 生成、解析、校验功能，用于无状态认证。
 * </p>
 *
 * @author talent-hr
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    /**
     * 构造方法，从配置文件注入密钥和过期时间
     *
     * @param secret     JWT 密钥
     * @param expiration 过期时间（毫秒）
     */
    public JwtUtil(@Value("${talent.jwt.secret}") String secret,
                   @Value("${talent.jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 JWT Token
     *
     * @param username 用户名
     * @return JWT Token 字符串
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 从 Token 中解析用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 校验 Token 是否有效
     *
     * @param token JWT Token
     * @return true 有效，false 无效或过期
     */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 解析 Token 的 Claims
     *
     * @param token JWT Token
     * @return Claims 对象
     */
    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
