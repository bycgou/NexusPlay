package com.biliplus.pojo.entity;

import lombok.Data;

@Data
public class AdminUser {
    private Integer id;
    private String account;   // 管理员登录账号
    private String name;        // 管理员姓名
    private String password;    // 管理员密码

}
