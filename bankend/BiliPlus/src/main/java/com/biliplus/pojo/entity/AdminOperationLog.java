package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 管理端操作日志。只读审计，不参与业务 */
@Data
public class AdminOperationLog {

    private Long id;
    /** 操作管理员；系统自动动作（如到期解封）为 null */
    private Long adminId;
    /** video.approve|user.ban|report.handle|... */
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private String ip;
    private LocalDateTime createTime;
}
