package com.biliplus.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// todo yaml 配置文件 读取 后续实现
@ConfigurationProperties(prefix = "biliplus.jwt")
@Data
@Component
public class JwtProperties {
    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;
}
