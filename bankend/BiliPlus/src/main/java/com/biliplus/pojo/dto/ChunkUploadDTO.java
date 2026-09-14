package com.biliplus.pojo.dto;

import lombok.Data;
// 分片上传请求参数（前端JSON序列化后通过formData传递）
@Data
public class ChunkUploadDTO {
    private String md5; // 文件唯一标识
    private Integer chunkIndex; // 当前分片索引
    private Integer totalChunks; // 总片数
    private String fileName; // 原文件名
    private String fileType; // 文件类型
}
