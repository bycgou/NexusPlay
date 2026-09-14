package com.biliplus.pojo.dto;

import lombok.Data;

// 合并请求参数
@Data
public class MergeDTO {
    private String md5;
    private String fileName;
    private Integer totalChunks;
}
