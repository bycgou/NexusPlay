package com.biliplus.utils;

/**
 * 静态资源 URL 规范化：把误写入库的 localhost 地址替换为对外 base。
 */
public final class MediaUrlUtil {

    private MediaUrlUtil() {
    }

    /**
     * @param url        原始 URL（可能含 http://localhost:8081/... ）
     * @param externalUrl 配置的对外基址，如 https://example.com
     */
    public static String rewrite(String url, String externalUrl) {
        if (url == null || url.isEmpty() || externalUrl == null || externalUrl.isEmpty()) {
            return url;
        }
        String base = externalUrl.endsWith("/")
                ? externalUrl.substring(0, externalUrl.length() - 1)
                : externalUrl;
        String lower = url.toLowerCase();
        if (lower.startsWith("http://localhost:8081")
                || lower.startsWith("http://127.0.0.1:8081")
                || lower.startsWith("https://localhost:8081")) {
            int idx = url.indexOf('/', url.indexOf("://") + 3);
            String path = idx >= 0 ? url.substring(idx) : "";
            return base + path;
        }
        return url;
    }
}
