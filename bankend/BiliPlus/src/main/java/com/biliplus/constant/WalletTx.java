package com.biliplus.constant;

/** 钱包账变的类型与业务类型取值，充值/送礼/主播收入共用，避免各处写魔法值 */
public final class WalletTx {

    /** 账变类型：1充值 2送礼支出 3主播收入 4系统调整 */
    public static final int TYPE_RECHARGE = 1;
    public static final int TYPE_GIFT = 2;
    public static final int TYPE_HOST_INCOME = 3;
    public static final int TYPE_ADJUST = 4;

    /** 业务类型，用于定位账变来源与余额口径 */
    public static final String BIZ_RECHARGE = "recharge";
    public static final String BIZ_GIFT = "gift";
    public static final String BIZ_HOST_INCOME = "host_income";
    public static final String BIZ_ADJUST = "adjust";

    private WalletTx() {
    }
}
