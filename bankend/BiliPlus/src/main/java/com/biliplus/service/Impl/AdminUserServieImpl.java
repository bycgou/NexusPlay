package com.biliplus.service.Impl;

import com.biliplus.mapper.AdminUserMapper;
import com.biliplus.pojo.dto.admindto.AdminUserLoginDTO;
import com.biliplus.pojo.entity.AdminUser;
import com.biliplus.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            log.warn("管理员登录失败：密码为空");
            return null;
        }

        // 调用mapper层方法
        AdminUser adminUser = adminUserMapper.login(adminAccount);
        if (adminUser == null || adminUser.getAccount() == null) {
            log.warn("管理员登录失败：账号不存在, account={}", adminAccount);
            return null;
        }

        // BCrypt 密码校验
//        if (!passwordEncoder.matches(adminPassword, adminUser.getPassword())) {
//            log.warn("管理员登录失败：密码错误, account={}", adminAccount);
//            return null;
//        }

        // 返回entity 实体
        log.info("管理员登录成功, id={}", adminUser.getId());
        return adminUser;
    }
}
