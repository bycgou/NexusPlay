package com.biliplus.constant;

/** 举报目标类型与状态 */
public final class ReportTarget {

    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_COMMENT = 2;
    public static final int TYPE_DANMAKU = 3;
    public static final int TYPE_USER = 4;
    public static final int TYPE_LIVE_ROOM = 5;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_HANDLED = 1;
    public static final int STATUS_REJECTED = 2;

    private ReportTarget() {
    }
}
