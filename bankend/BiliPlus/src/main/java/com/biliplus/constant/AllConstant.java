package com.biliplus.constant;

public class AllConstant {
    public static final Integer LENGTH_5 = 5;
    // 验证码Redis存储前缀（抽取为常量，便于统一管理和修改）
    public static final String CAPTCHA_REDIS_PREFIX = "code:validate:";
}
