package com.biliplus.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class JwtUtil {

    private static final int MIN_SECRET_LENGTH = 32;

    private static final Set<String> FORBIDDEN_SECRETS = Set.of(
            "biliPlusAdminSecretKey2024",
            "biliPlusPeopleSecretKey2024",
            "change-me-admin",
            "change-me-people"
    );

    /**
     * 生成 jwt（HS256）
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        validateSecretKey(secretKey);

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Date exp = new Date(System.currentTimeMillis() + ttlMillis);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token 解析
     */
    public static Claims parseJWT(String secretKey, String token) {
        validateSecretKey(secretKey);

        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token).getBody();
    }

    /**
     * 校验密钥强度，拒绝弱默认值
     */
    private static void validateSecretKey(String secretKey) {
        if (secretKey == null || secretKey.trim().length() < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                    "JWT secret key too short. Minimum length is " + MIN_SECRET_LENGTH + " characters.");
        }
        String trimmed = secretKey.trim();
        if (FORBIDDEN_SECRETS.contains(trimmed)) {
            throw new IllegalArgumentException("JWT secret key is a known weak default. Refusing to use it.");
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.contains("change-me") || lower.contains("secretkey2024") || lower.contains("password")) {
            throw new IllegalArgumentException("JWT secret key looks like a placeholder/weak default. Refusing to use it.");
        }
    }

    /**
     * 从 Claims 中安全提取 userId（兼容 Integer/Long 类型）
     */
    public static Long extractUserId(Claims claims) {
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }

    /**
     * 从 Claims 中安全提取 adminId（兼容 Integer/Long 类型）
     */
    public static Long extractAdminId(Claims claims) {
        Object adminIdObj = claims.get("adminId");
        if (adminIdObj instanceof Integer) {
            return ((Integer) adminIdObj).longValue();
        } else if (adminIdObj instanceof Long) {
            return (Long) adminIdObj;
        } else if (adminIdObj instanceof Number) {
            return ((Number) adminIdObj).longValue();
        }
        return null;
    }
}
