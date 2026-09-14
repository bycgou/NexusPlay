package com.biliplus.service;

import java.util.Map;

/**
 * 验证码服务
 */
public interface ValidateCodeService {
    Map<String, String> generateValidateCode();
    boolean validateCode(String captchaId, String userInputCode);
}
