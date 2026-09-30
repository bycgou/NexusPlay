package com.biliplus.service;

import com.biliplus.mapper.AdminOperationLogMapper;
import com.biliplus.pojo.entity.AdminOperationLog;
import com.biliplus.service.Impl.AdminOperationLogServiceImpl;
import com.biliplus.utils.AdminContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOperationLogServiceImplTest {

    @Mock
    private AdminOperationLogMapper adminOperationLogMapper;

    @InjectMocks
    private AdminOperationLogServiceImpl adminOperationLogService;

    @AfterEach
    void tearDown() {
        AdminContext.clear();
    }

    @Test
    void record_shouldTakeAdminIdAndIpFromContext() {
        AdminContext.setAdminId(7L);
        AdminContext.setClientIp("10.0.0.8");

        adminOperationLogService.record("video.approve", "video", 3L, "通过");

        ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
        verify(adminOperationLogMapper).insert(captor.capture());
        AdminOperationLog saved = captor.getValue();
        assertEquals(7L, saved.getAdminId());
        assertEquals("10.0.0.8", saved.getIp());
        assertEquals("video.approve", saved.getAction());
        assertEquals("video", saved.getTargetType());
        assertEquals(3L, saved.getTargetId());
        assertNotNull(saved.getCreateTime());
    }

    @Test
    void record_whenSystemAction_shouldAllowNullAdminId() {
        adminOperationLogService.record("user.penalty.auto", "user", 9L, "系统自动禁言");

        ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
        verify(adminOperationLogMapper).insert(captor.capture());
        assertNull(captor.getValue().getAdminId());
    }

    @Test
    void record_whenDetailTooLong_shouldTruncate() {
        adminOperationLogService.record("report.handle", "report", 1L, "x".repeat(800));

        ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
        verify(adminOperationLogMapper).insert(captor.capture());
        assertEquals(500, captor.getValue().getDetail().length());
    }

    @Test
    void record_whenMapperFails_shouldNotPropagate() {
        doThrow(new RuntimeException("db down")).when(adminOperationLogMapper).insert(any());

        // 旁路写入，绝不能影响管理操作本身
        assertDoesNotThrow(() -> adminOperationLogService.record("video.approve", "video", 1L, null));
    }

    @Test
    void record_whenActionBlank_shouldSkip() {
        adminOperationLogService.record(null, "video", 1L, null);
        adminOperationLogService.record("  ", "video", 1L, null);
        verify(adminOperationLogMapper, never()).insert(any());
    }
}
