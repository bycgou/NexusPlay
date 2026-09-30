package com.biliplus.service;

/**
 * 行为流水写入。所有埋点统一走这里，
 * 写入失败只打日志、不影响主业务（与通知写入同一策略）。
 */
public interface EventLogService {

    void record(String eventType, Long userId, String targetType, Long targetId);

    void record(String eventType, Long userId, String targetType, Long targetId,
                Integer durationSec, String extra, String source);
}
