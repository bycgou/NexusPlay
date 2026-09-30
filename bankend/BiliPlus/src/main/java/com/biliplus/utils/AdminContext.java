package com.biliplus.utils;

/**
 * 管理端请求上下文：adminId 与来源 IP。
 * 由 JwtTokenAdminInterceptor 在鉴权时注入，ThreadLocalCleanupInterceptor 清理，
 * 操作日志埋点直接读取，避免把 adminId/ip 一路透传到 Service 签名里。
 */
public final class AdminContext {

    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CLIENT_IP = new ThreadLocal<>();

    private AdminContext() {
    }

    public static void setAdminId(Long adminId) {
        ADMIN_ID.set(adminId);
    }

    public static Long getAdminId() {
        return ADMIN_ID.get();
    }

    public static void setClientIp(String ip) {
        CLIENT_IP.set(ip);
    }

    public static String getClientIp() {
        return CLIENT_IP.get();
    }

    public static void clear() {
        ADMIN_ID.remove();
        CLIENT_IP.remove();
    }
}
