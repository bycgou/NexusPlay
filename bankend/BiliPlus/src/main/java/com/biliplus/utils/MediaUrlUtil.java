package com.biliplus.utils;

import java.util.regex.Pattern;

/**
 * 静态资源 URL 规范化：把误写入库的 localhost 地址替换为对外 base。
 */
public final class MediaUrlUtil {

    /** 本机地址（含任意端口），如 http://localhost:8081/... 或 http://127.0.0.1:21160/... */
    private static final Pattern LOCAL_ORIGIN =
            Pattern.compile("^https?://(?:localhost|127\\.0\\.0\\.1)(?::\\d+)?", Pattern.CASE_INSENSITIVE);

    private MediaUrlUtil() {
    }

    /**
     * @param url        原始 URL（可能含 http://localhost:21160/... ）
     * @param externalUrl 配置的对外基址，如 https://example.com
     */
    public static String rewrite(String url, String externalUrl) {
        if (url == null || url.isEmpty() || externalUrl == null || externalUrl.isEmpty()) {
            return url;
        }
        String base = externalUrl.endsWith("/")
                ? externalUrl.substring(0, externalUrl.length() - 1)
                : externalUrl;
        if (!LOCAL_ORIGIN.matcher(url).find()) {
            return url;
        }
        int idx = url.indexOf('/', url.indexOf("://") + 3);
        String path = idx >= 0 ? url.substring(idx) : "";
        return base + path;
    }
}
