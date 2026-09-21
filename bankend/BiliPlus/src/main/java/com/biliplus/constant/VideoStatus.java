package com.biliplus.constant;

/**
 * 稿件状态。管理端审核、用户端稿件管理共用一套取值，避免两端各写一份导致状态语义漂移。
 */
public final class VideoStatus {

    /** 用户软删除，前台与管理端均不可见 */
    public static final int DELETED = -1;
    /** 待审核 */
    public static final int PENDING = 0;
    /** 审核通过 / 正常 */
    public static final int NORMAL = 1;
    /** 已下架 */
    public static final int OFFLINE = 2;
    /** 审核不通过 */
    public static final int REJECTED = 3;

    private VideoStatus() {
    }
}
