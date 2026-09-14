package com.biliplus.service.Impl;

import com.biliplus.constant.AllConstant;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.service.EmailCodeService;
import com.biliplus.utils.StringTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor // 替代@Autowired，通过构造器注入依赖（更规范）
public class EmailCodeServiceImpl implements EmailCodeService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSenderImpl mailSender;
    private final PeopleUserMapper peopleUserMapper;

    @Value("${spring.mail.username}")
    private String senderEmail; // 发送者邮箱（变量名更清晰）

    @Override
    public void sendEmailCode(String email) {
        // 1. 参数校验（防止空指针和无效参数）
        Assert.hasText(email, "邮箱不能为空");

        // 3. 处理类型1：发送注册验证码

            // 3.1 生成5位随机验证码
            String code = StringTools.getRandomNumber(AllConstant.LENGTH_5);
            log.debug("生成邮箱验证码：{}，发送至：{}", code, email); // 生产环境可去掉code日志

            // 3.2 构建邮件消息
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail); // 发送者邮箱（必须与配置的spring.mail.username一致）
            message.setTo(email);        // 接收者邮箱
            message.setSubject("【BiliPlus】注册验证码"); // 邮件主题加标识，避免进入垃圾邮件
            message.setText(String.format(
                    "您正在注册BiliPlus账号，验证码为：%s\n有效期3分钟，请不要泄露给他人。",
                    code
            )); // 验证码内容格式化

            try {
                // 3.3 发送邮件（捕获发送异常）
                mailSender.send(message);
                log.info("验证码已发送至邮箱：{}", email);
            } catch (MailException e) {
                log.error("发送邮件失败，邮箱：{}", email, e);
                throw new RuntimeException("邮件发送失败，请检查邮箱是否有效");
            }

            // 3.4 验证码存入Redis（设置3分钟过期）
            // key 需与 PeopleUserServiceImpl 注册时读取的一致：email:verify:{email}
            String redisKey = String.format("email:verify:%s", email);
            stringRedisTemplate.opsForValue().set(redisKey, code, 3, TimeUnit.MINUTES);
            log.info("验证码已存入Redis，key：{}", redisKey);

    }

}