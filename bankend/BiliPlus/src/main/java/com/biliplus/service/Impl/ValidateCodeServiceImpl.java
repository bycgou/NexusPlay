package com.biliplus.service.Impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.biliplus.constant.AllConstant;
import com.biliplus.service.ValidateCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ValidateCodeServiceImpl implements ValidateCodeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Map<String, String> generateValidateCode() {
        try {
            CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(150, 48, 4, 20);
            String codeValue = circleCaptcha.getCode().toLowerCase();
            // 不打印验证码明文，避免日志泄露
            log.debug("生成图片验证码成功");

            String captchaId = UUID.randomUUID().toString().replaceAll("-", "");
            String redisKey = AllConstant.CAPTCHA_REDIS_PREFIX + captchaId;
            stringRedisTemplate.opsForValue().set(redisKey, codeValue, AllConstant.LENGTH_5, TimeUnit.MINUTES);

            String imageBase64 = "data:image/png;base64," + circleCaptcha.getImageBase64();
            Map<String, String> result = new HashMap<>(2);
            result.put("captchaId", captchaId);
            result.put("imageBase64", imageBase64);
            return result;
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            throw new RuntimeException("验证码生成失败，请稍后重试", e);
        }
    }

    /**
     * 校验验证码。无论对错都会删除（一次性），防止同一 captchaId 被爆破。
     */
    @Override
    public boolean validateCode(String captchaId, String userInputCode) {
        if (captchaId == null || userInputCode == null) {
            log.warn("验证码ID或用户输入为空");
            return false;
        }

        String failKey = AllConstant.CAPTCHA_FAIL_PREFIX + captchaId;
        String failStr = stringRedisTemplate.opsForValue().get(failKey);
        int fails = 0;
        if (failStr != null) {
            try {
                fails = Integer.parseInt(failStr);
            } catch (NumberFormatException ignored) {
            }
        }
        if (fails >= AllConstant.CAPTCHA_MAX_FAIL) {
            stringRedisTemplate.delete(AllConstant.CAPTCHA_REDIS_PREFIX + captchaId);
            log.warn("验证码失败次数过多，captchaId={}", captchaId);
            return false;
        }

        String redisKey = AllConstant.CAPTCHA_REDIS_PREFIX + captchaId;
        // 一次性：取出即删，失败也不可重试
        String realCode = stringRedisTemplate.opsForValue().getAndDelete(redisKey);
        if (realCode == null) {
            log.warn("验证码已过期或不存在，captchaId：{}", captchaId);
            return false;
        }

        boolean isMatch = realCode.equals(userInputCode.toLowerCase().trim());
        if (!isMatch) {
            stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, 5, TimeUnit.MINUTES);
            log.warn("验证码输入错误，captchaId：{}", captchaId);
        } else {
            stringRedisTemplate.delete(failKey);
        }
        return isMatch;
    }
}
