package com.biliplus.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 解析。仅在配置了可信反向代理时才使用 X-Forwarded-For 的第一段。
 */
public final class ClientIpUtil {

    private ClientIpUtil() {
    }

    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        return request.getRemoteAddr();
    }
}
