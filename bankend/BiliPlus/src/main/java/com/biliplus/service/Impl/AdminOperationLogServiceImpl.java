package com.biliplus.service.Impl;

import com.biliplus.mapper.AdminOperationLogMapper;
import com.biliplus.pojo.entity.AdminOperationLog;
import com.biliplus.service.AdminOperationLogService;
import com.biliplus.utils.AdminContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** 操作日志是旁路逻辑：写失败只打日志，绝不影响管理操作本身 */
@Slf4j
@Service
public class AdminOperationLogServiceImpl implements AdminOperationLogService {

    private static final int MAX_DETAIL = 500;

    @Autowired
    private AdminOperationLogMapper adminOperationLogMapper;

    @Override
    public void record(String action, String targetType, Long targetId, String detail) {
        try {
            if (action == null || action.isBlank()) {
                return;
            }
            AdminOperationLog record = new AdminOperationLog();
            record.setAdminId(AdminContext.getAdminId());
            record.setAction(action);
            record.setTargetType(targetType);
            record.setTargetId(targetId);
            record.setDetail(truncate(detail));
            record.setIp(AdminContext.getClientIp());
            record.setCreateTime(LocalDateTime.now());
            adminOperationLogMapper.insert(record);
        } catch (Exception e) {
            log.warn("写操作日志失败 action={}, target={}/{}", action, targetType, targetId, e);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_DETAIL ? value : value.substring(0, MAX_DETAIL);
    }
}
