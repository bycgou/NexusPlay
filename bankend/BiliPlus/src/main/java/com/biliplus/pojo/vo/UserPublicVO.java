package com.biliplus.pojo.vo;

import lombok.Data;

/**
 * 公开用户资料（不含邮箱/手机号等敏感字段）
 */
@Data
public class UserPublicVO {
    private Long id;
    private String username;
    private String avatar;
    private String nickname;
    private String signature;
    private Long fansCount;
    private Long followingCount;
}
