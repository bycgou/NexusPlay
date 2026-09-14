package com.biliplus.controller.admin;

import com.biliplus.pojo.dto.admindto.AdminUserDTO;
import com.biliplus.pojo.dto.admindto.AdminUserLoginDTO;
import com.biliplus.pojo.entity.AdminUser;
import com.biliplus.pojo.vo.AdminUserLoginVO;
import com.biliplus.properties.JwtProperties;
import com.biliplus.result.Result;
import com.biliplus.service.AdminUserService;
import com.biliplus.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 登录
    @PostMapping("/login")
    public Result<AdminUserLoginVO> login(@RequestBody AdminUserLoginDTO adminUserLoginDTO){
        log.info("管理员登录请求: account={}", adminUserLoginDTO.getAccount());

        // 调用业务层登录方法,返回adminUser实体
        AdminUser adminUser = adminUserService.login(adminUserLoginDTO);
        if (adminUser == null) {
            return Result.error("账号或密码错误");
        }

        // 生成jwt令牌，claims 中写入 adminId
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

        return Result.success(adminUserLoginVO);
    }

    // 注册
    @PostMapping()
    public Result register(@RequestBody AdminUserDTO adminUserDTO){
        log.info("管理员注册:{}", adminUserDTO);
        // TODO 注册
        return Result.success();
    }

}
