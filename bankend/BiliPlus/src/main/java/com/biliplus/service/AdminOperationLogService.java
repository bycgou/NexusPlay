package com.biliplus.service;

/**
 * 管理端操作日志。与行为流水同策略：写失败只打日志，绝不影响主业务。
 * adminId 与 IP 取自 AdminContext（拦截器注入）。
 */
public interface AdminOperationLogService {

    void record(String action, String targetType, Long targetId, String detail);
}
