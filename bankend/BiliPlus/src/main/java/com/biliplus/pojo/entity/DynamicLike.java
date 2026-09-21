package com.biliplus.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DynamicLike {

    private Long id;
    private Long userId;
    private Long dynamicId;
    private LocalDateTime createTime;
}
