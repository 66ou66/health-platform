package edu.dlnu.health_platform.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成/解析登录令牌
 */
@Component
public class JwtUtils {

    /** 密钥（application.yml 中配置，HS256 要求至少 32 字节） */
    @Value("${jwt.secret}")
    private String secret;

    /** 过期时间（毫秒） */
    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成令牌，载荷中携带 userId 和 role（0管理员 1患者 2志愿者） */
    public String generateToken(Long userId, String userName, Integer role) {
        return Jwts.builder()
                .subject(userName)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /** 解析令牌，返回载荷（过期/伪造会抛异常，由调用方处理） */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
