package com.biliplus.service.Impl;

import com.biliplus.constant.EventType;
import com.biliplus.mapper.EventLogMapper;
import com.biliplus.pojo.entity.EventLog;
import com.biliplus.service.EventLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 行为流水写入是旁路逻辑：
 * 记录失败只打日志，绝不向调用方抛出，也绝不阻断主业务。
 */
@Slf4j
@Service
public class EventLogServiceImpl implements EventLogService {

    /** extra 最长 500，超长截断，避免一条超长评论打挂流水表 */
    private static final int MAX_EXTRA = 500;

    @Autowired
    private EventLogMapper eventLogMapper;

    @Override
    public void record(String eventType, Long userId, String targetType, Long targetId) {
        record(eventType, userId, targetType, targetId, null, null, EventType.SOURCE_WEB);
    }

    @Override
    public void record(String eventType, Long userId, String targetType, Long targetId,
                       Integer durationSec, String extra, String source) {
        try {
            if (eventType == null || eventType.isBlank()) {
                return;
            }
            EventLog event = new EventLog();
            event.setUserId(userId);
            event.setEventType(eventType);
            event.setTargetType(targetType);
            event.setTargetId(targetId);
            event.setDurationSec(durationSec);
            event.setExtra(truncate(extra));
            event.setSource(source == null ? EventType.SOURCE_WEB : source);
            event.setCreateTime(LocalDateTime.now());
            eventLogMapper.insert(event);
        } catch (Exception e) {
            log.warn("写行为流水失败 eventType={}, target={}/{}", eventType, targetType, targetId, e);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_EXTRA ? value : value.substring(0, MAX_EXTRA);
    }
}
