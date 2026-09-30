package com.biliplus.constant;

/** 行为流水事件类型。推荐特征、治理报表、协同实验都吃这套枚举 */
public final class EventType {

    // 内容消费
    public static final String VIDEO_VIEW = "video_view";
    public static final String VIDEO_SHARE = "video_share";

    // 互动
    public static final String VIDEO_LIKE = "video_like";
    public static final String VIDEO_UNLIKE = "video_unlike";
    public static final String VIDEO_FAVORITE = "video_favorite";
    public static final String VIDEO_UNFAVORITE = "video_unfavorite";
    public static final String COMMENT = "comment";
    public static final String DANMAKU = "danmaku";
    public static final String FOLLOW = "follow";
    public static final String UNFOLLOW = "unfollow";

    // 直播与打赏
    public static final String GIFT_SEND = "gift_send";
    public static final String LIVE_ENTER = "live_enter";
    public static final String LIVE_WATCH = "live_watch";

    // 治理
    public static final String REPORT = "report";
    public static final String AUDIT_PASS = "audit_pass";
    public static final String AUDIT_REJECT = "audit_reject";
    public static final String AUDIT_OFFLINE = "audit_offline";
    public static final String USER_BAN = "user_ban";

    // 目标类型
    public static final String TARGET_VIDEO = "video";
    public static final String TARGET_USER = "user";
    public static final String TARGET_COMMENT = "comment";
    public static final String TARGET_LIVE_ROOM = "live_room";
    public static final String TARGET_GIFT = "gift";
    public static final String TARGET_DYNAMIC = "dynamic";

    // 来源
    public static final String SOURCE_WEB = "web";
    public static final String SOURCE_ADMIN = "admin";
    public static final String SOURCE_SYSTEM = "system";

    private EventType() {
    }
}
