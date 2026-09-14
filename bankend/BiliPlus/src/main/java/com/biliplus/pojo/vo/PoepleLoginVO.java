package com.biliplus.pojo.vo;

import lombok.Data;

@Data
public class PoepleLoginVO {
    private Long id;  // id
    private String username; //名字
    private String email; // 邮箱
    private String phone;  // 手机号
    private String avatar; // 头像url
    private String nickname; // 昵称
    private String signature; // 个性签名
    private Integer role; // 角色
    private Integer status; // 状态
    private String token; // token



}
