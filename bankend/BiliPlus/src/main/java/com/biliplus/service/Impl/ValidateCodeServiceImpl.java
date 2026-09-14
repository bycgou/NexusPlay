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

    // Redis模板（通过构造器注入，final修饰确保不可变）
    @Autowired
    private  StringRedisTemplate stringRedisTemplate;




    @Override
    public Map<String, String> generateValidateCode() {
        try {
            // 1. 生成图片验证码（使用Hutool的CircleCaptcha，参数：宽、高、字符数、干扰线数）
            CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(150, 48, 4, 20);

            // 2. 获取验证码值（建议转为小写，降低前端输入大小写敏感度）
            String codeValue = circleCaptcha.getCode().toLowerCase();
            log.info("生成验证码：{}", codeValue); // 生产环境可去掉明文日志，或加密输出

            // 3. 生成唯一标识（UUID），作为前端传递的验证码ID
            String captchaId = UUID.randomUUID().toString().replaceAll("-", "");

            // 4. 生成Redis存储键（前缀+唯一标识，避免键冲突）
            String redisKey = AllConstant.CAPTCHA_REDIS_PREFIX+ captchaId;

            // 5. 存入Redis并设置过期时间（5分钟）
            stringRedisTemplate.opsForValue().set(redisKey, codeValue, AllConstant.LENGTH_5, TimeUnit.MINUTES);
            log.debug("验证码存入Redis，key：{}，有效期：{}分钟", redisKey, AllConstant.LENGTH_5);

            // 6. 生成图片Base64编码（拼接data协议头，前端可直接作为img的src）
            String imageBase64 = "data:image/png;base64," + circleCaptcha.getImageBase64();

            // 7. 封装返回结果（使用HashMap，避免Map.of的不可变性限制）
            Map<String, String> result = new HashMap<>(2);
            result.put("captchaId", captchaId); // 推荐用captchaId替代key，语义更清晰
            result.put("imageBase64", imageBase64);
            return result;

        } catch (Exception e) {
            // 捕获异常并封装，避免底层异常直接暴露给上层
            log.error("生成验证码失败", e);
            throw new RuntimeException("验证码生成失败，请稍后重试", e);
        }
    }

    /**
     * 验证验证码是否正确
     * @param captchaId 验证码唯一标识（生成时返回的captchaId）
     * @param userInputCode 用户输入的验证码
     * @return 验证结果：true-正确，false-错误或过期
     */
    @Override
    public boolean validateCode(String captchaId, String userInputCode) {
        if (captchaId == null || userInputCode == null) {
            log.warn("验证码ID或用户输入为空");
            return false;
        }

        // 1. 构建Redis键
        String redisKey = AllConstant.CAPTCHA_REDIS_PREFIX + captchaId;

        // 2. 从Redis获取真实验证码（并删除，防止重复使用）
        String realCode = stringRedisTemplate.opsForValue().getAndDelete(redisKey);
        if (realCode == null) {
            log.warn("验证码已过期或不存在，captchaId：{}", captchaId);
            return false;
        }

        // 3. 比较（忽略大小写）
        boolean isMatch = realCode.equals(userInputCode.toLowerCase());
        if (!isMatch) {
            log.warn("验证码输入错误，用户输入：{}，正确值：{}", userInputCode, realCode);
        }
        return isMatch;
    }
}
