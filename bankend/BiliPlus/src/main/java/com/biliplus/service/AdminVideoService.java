package com.biliplus.service;

import com.biliplus.result.PageResult;

public interface AdminVideoService {

    PageResult pageByStatus(Integer status, Integer page, Integer pageSize);

    com.biliplus.pojo.entity.Video getById(Long videoId);

    void approve(Long videoId);

    /** 审核不通过，需填写原因（status 0 -> 3） */
    void reject(Long videoId, String reason);

    void offline(Long videoId);
}
