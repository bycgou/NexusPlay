package com.biliplus.pojo.dto.userdto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String email;  // 邮箱
    private String captchaId; // 图片验证码的标识
    private String imageCaptcha; // 图片验证码
    private String password;  // 密码
    private String emailCaptchaId; // 邮箱验证码的标识
    private String emailCaptcha; // 邮箱验证码
}
