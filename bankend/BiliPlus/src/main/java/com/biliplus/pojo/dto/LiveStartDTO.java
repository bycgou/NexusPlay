package com.biliplus.pojo.dto;

import lombok.Data;

/** 开播请求 */
@Data
public class LiveStartDTO {
    private String title;
    private String coverUrl;
    private Integer categoryId;
}
