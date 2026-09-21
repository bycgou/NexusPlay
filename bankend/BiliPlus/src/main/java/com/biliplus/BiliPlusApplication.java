package com.biliplus;  // 确保包路径与你的项目一致

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;  // 关键：导入Spring的Environment
import org.springframework.scheduling.annotation.EnableScheduling;


@MapperScan("com.biliplus.mapper")
@SpringBootApplication
@EnableScheduling  // PK 到期自动结束等定时任务
public class BiliPlusApplication {

    // 注入Spring的Environment（而非MyBatis的）
    @Resource
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(BiliPlusApplication.class, args);
    }

    // 项目启动后打印配置，验证是否加载成功
    @PostConstruct
    public void printDataSourceConfig() {
        System.out.println("=== 数据源配置读取结果 ===");
        // 关键：去掉 .druid 后缀（Hikari 配置无 druid 层级）
        System.out.println("url: " + env.getProperty("spring.datasource.url"));
        System.out.println("driver: " + env.getProperty("spring.datasource.driver-class-name"));
        System.out.println("username: " + env.getProperty("spring.datasource.username"));
    }
}
