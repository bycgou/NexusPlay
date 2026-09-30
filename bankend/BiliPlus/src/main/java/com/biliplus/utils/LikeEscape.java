package com.biliplus.utils;

/**
 * SQL LIKE 通配符转义，防止 % _ \ 被当作模式匹配。
 * 配合 MySQL 默认转义符 `\` 使用。
 */
public final class LikeEscape {

    private LikeEscape() {
    }

    public static String escape(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        return raw
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /** 搜索关键字：trim + 最小长度 + LIKE 转义 */
    public static String prepareKeyword(String raw) {
        if (raw == null) {
            return null;
        }
        String kw = raw.trim();
        if (kw.isEmpty()) {
            return null;
        }
        return escape(kw);
    }
}
