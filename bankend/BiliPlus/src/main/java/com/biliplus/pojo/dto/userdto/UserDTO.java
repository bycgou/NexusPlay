package com.biliplus.pojo.dto.userdto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;  //   用户名类似bilibili的UID
    private String email;
    private String phone;
    private String avatar;  //   头像URL
    private String nickname; //  昵称
    private String signature; // 个性签名
    private Long fansCount; // 粉丝数（接口补充返回）
    private Long followingCount; // 关注数
}
