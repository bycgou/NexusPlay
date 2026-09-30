package com.biliplus.controller.admin;

import com.biliplus.pojo.dto.admindto.AdminUserDTO;
import com.biliplus.pojo.dto.admindto.AdminUserLoginDTO;
import com.biliplus.pojo.entity.AdminUser;
import com.biliplus.pojo.vo.AdminUserLoginVO;
import com.biliplus.properties.JwtProperties;
import com.biliplus.result.Result;
import com.biliplus.service.AdminUserService;
import com.biliplus.utils.ClientIpUtil;
import com.biliplus.utils.JwtUtil;
import com.biliplus.utils.LoginRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @PostMapping("/login")
    public ResponseEntity<Result<AdminUserLoginVO>> login(@RequestBody AdminUserLoginDTO adminUserLoginDTO,
                                                          jakarta.servlet.http.HttpServletRequest request) {
        String account = adminUserLoginDTO == null ? null : adminUserLoginDTO.getAccount();
        log.info("管理员登录请求: account={}", account);

        if (account == null || account.trim().isEmpty()
                || adminUserLoginDTO.getPassword() == null || adminUserLoginDTO.getPassword().isEmpty()) {
            return ResponseEntity.status(401).body(Result.error("账号或密码错误"));
        }

        String ip = ClientIpUtil.resolve(request);
        if (!loginRateLimiter.allowLoginIp(ip)) {
            log.warn("管理员登录 IP 触发限速 ip={}", ip);
            return ResponseEntity.status(429).body(Result.error("尝试次数过多，请稍后再试"));
        }

        String rlKey = "admin-login:" + account.trim().toLowerCase();
        if (!loginRateLimiter.allow(rlKey, 5, Duration.ofMinutes(15))) {
            log.warn("管理员登录触发限速 account={}", account);
            return ResponseEntity.status(429).body(Result.error("尝试次数过多，请稍后再试"));
        }

        AdminUser adminUser = adminUserService.login(adminUserLoginDTO);
        if (adminUser == null) {
            return ResponseEntity.status(401).body(Result.error("账号或密码错误"));
        }
        loginRateLimiter.reset(rlKey);

        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminUser.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );

        AdminUserLoginVO adminUserLoginVO = new AdminUserLoginVO();
        adminUserLoginVO.setId(adminUser.getId());
        adminUserLoginVO.setAccount(adminUser.getAccount());
        adminUserLoginVO.setName(adminUser.getName());
        adminUserLoginVO.setToken(token);
        log.info("管理员登录成功: id={}, account={}", adminUser.getId(), adminUser.getAccount());

        return ResponseEntity.ok(Result.success(adminUserLoginVO));
    }

    @PostMapping()
    public ResponseEntity<Result<Void>> register(@RequestBody AdminUserDTO adminUserDTO) {
        log.info("管理员注册请求");
        // 生产应关闭或走受控流程
        return ResponseEntity.status(403).body(Result.error("注册接口未开放"));
    }
}
