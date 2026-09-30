package com.biliplus.service.Impl;

import com.biliplus.mapper.AdminUserMapper;
import com.biliplus.pojo.dto.admindto.AdminUserLoginDTO;
import com.biliplus.pojo.entity.AdminUser;
import com.biliplus.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
public class AdminUserServieImpl implements AdminUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AdminUser login(AdminUserLoginDTO adminUserLoginDTO) {
        String adminAccount = adminUserLoginDTO.getAccount();
        String adminPassword = adminUserLoginDTO.getPassword();

        if (adminAccount == null || adminAccount.trim().isEmpty()) {
            log.warn("管理员登录失败：账号为空");
            return null;
        }
        if (adminPassword == null || adminPassword.isEmpty()) {
            log.warn("管理员登录失败：密码为空");
            return null;
        }

        AdminUser adminUser = adminUserMapper.login(adminAccount.trim());
        if (adminUser == null || adminUser.getAccount() == null) {
            log.warn("管理员登录失败：账号不存在 account={}", adminAccount);
            return null;
        }

        String stored = adminUser.getPassword();
        if (stored == null || stored.isEmpty()) {
            log.warn("管理员登录失败：密码未初始化 account={}", adminAccount);
            return null;
        }

        boolean ok;
        if (isBcryptHash(stored)) {
            ok = passwordEncoder.matches(adminPassword, stored);
        } else {
            // 兼容历史明文（仅迁移期）：常量时间比较，命中后升级为 BCrypt
            ok = constantTimeEquals(adminPassword, stored);
            if (ok) {
                String encoded = passwordEncoder.encode(adminPassword);
                adminUser.setPassword(encoded);
                try {
                    adminUserMapper.updatePassword(adminUser.getId().longValue(), encoded);
                    log.warn("管理员 {} 明文密码已升级为 BCrypt，请尽快人工复核并轮换", adminAccount);
                } catch (Exception e) {
                    log.error("管理员密码哈希回写失败 account={}", adminAccount, e);
                }
            }
        }

        if (!ok) {
            log.warn("管理员登录失败：密码错误 account={}", adminAccount);
            return null;
        }

        log.info("管理员登录成功 id={}", adminUser.getId());
        return adminUser;
    }

    private static boolean isBcryptHash(String stored) {
        return stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
