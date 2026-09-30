package com.biliplus.constant;

public class AllConstant {
    public static final Integer LENGTH_5 = 5;
    // 验证码Redis存储前缀（抽取为常量，便于统一管理和修改）
    public static final String CAPTCHA_REDIS_PREFIX = "code:validate:";
    /** 验证码失败计数前缀，用于限制同一 captchaId 的尝试次数 */
    public static final String CAPTCHA_FAIL_PREFIX = "code:validate:fail:";
    /** 允许的最大验证码失败次数，超过后强制作废 */
    public static final int CAPTCHA_MAX_FAIL = 3;
}
