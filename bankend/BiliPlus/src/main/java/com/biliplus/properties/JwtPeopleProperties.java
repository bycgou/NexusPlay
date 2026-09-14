package com.biliplus.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "biliplus.peoplejwt")
@Data
@Component
public class JwtPeopleProperties {
    /**
     * 用户生成jwt令牌相关配置
     */
    private String peopleSecretKey;
    private long peopleTtl;
    private String peopleTokenName;
}
