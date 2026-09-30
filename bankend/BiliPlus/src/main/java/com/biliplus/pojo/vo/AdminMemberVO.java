package com.biliplus.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 管理端用户列表/详情行 */
@Data
public class AdminMemberVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String signature;
    /** 0普通 1UP 2管理员 */
    private Integer role;
    /** 0禁用 1正常 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 信用分（无记录时默认 100） */
    private Integer creditScore;
    private Integer violationCount;
    private LocalDateTime lastViolationTime;

    private Long videoCount;
    private Long fansCount;
    private Long followingCount;

    /** 生效中禁言/封禁 */
    private Boolean muted;
    private Boolean banned;
}
