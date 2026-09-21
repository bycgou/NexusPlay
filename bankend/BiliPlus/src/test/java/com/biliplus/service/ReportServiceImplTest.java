package com.biliplus.service;

import com.biliplus.constant.ReportTarget;
import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.AdminVideoMapper;
import com.biliplus.mapper.CommentMapper;
import com.biliplus.mapper.ReportMapper;
import com.biliplus.pojo.entity.Comment;
import com.biliplus.pojo.entity.Report;
import com.biliplus.pojo.entity.Video;
import com.biliplus.service.Impl.ReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private AdminVideoMapper adminVideoMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Report pendingReport(int targetType, long targetId) {
        Report report = new Report();
        report.setId(5L);
        report.setReporterId(9L);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setStatus(ReportTarget.STATUS_PENDING);
        return report;
    }

    @Test
    void submit_whenNotLogin_shouldThrow() {
        assertThrows(BusinessException.class, () -> reportService.submit(null, 1, 1L, 1, null));
    }

    @Test
    void submit_whenReasonOutOfRange_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> reportService.submit(9L, ReportTarget.TYPE_VIDEO, 1L, 9, null));
        verify(reportMapper, never()).insert(any());
    }

    @Test
    void submit_whenTargetTypeIllegal_shouldThrow() {
        assertThrows(BusinessException.class, () -> reportService.submit(9L, 99, 1L, 1, null));
        verify(reportMapper, never()).insert(any());
    }

    @Test
    void submit_whenDuplicatePending_shouldThrow() {
        when(reportMapper.countPending(9L, ReportTarget.TYPE_VIDEO, 3L)).thenReturn(1);

        assertThrows(BusinessException.class,
                () -> reportService.submit(9L, ReportTarget.TYPE_VIDEO, 3L, 4, "广告"));
        verify(reportMapper, never()).insert(any());
    }

    @Test
    void submit_shouldInsert() {
        when(reportMapper.countPending(9L, ReportTarget.TYPE_COMMENT, 3L)).thenReturn(0);

        Report saved = reportService.submit(9L, ReportTarget.TYPE_COMMENT, 3L, 3, " 骂人 ");

        assertEquals(9L, saved.getReporterId());
        assertEquals(ReportTarget.TYPE_COMMENT, saved.getTargetType());
        assertEquals(3L, saved.getTargetId());
        assertEquals(3, saved.getReason());
        assertEquals("骂人", saved.getDetail());
        verify(reportMapper).insert(any(Report.class));
    }

    @Test
    void handle_whenAlreadyHandled_shouldThrow() {
        Report handled = pendingReport(ReportTarget.TYPE_VIDEO, 3L);
        handled.setStatus(ReportTarget.STATUS_HANDLED);
        when(reportMapper.selectById(5L)).thenReturn(handled);

        assertThrows(BusinessException.class,
                () -> reportService.handle(1L, 5L, ReportTarget.STATUS_HANDLED, "ok"));
        verify(reportMapper, never()).handle(any(), any(), any(), any(), any());
    }

    @Test
    void handle_whenStatusIllegal_shouldThrow() {
        assertThrows(BusinessException.class, () -> reportService.handle(1L, 5L, 0, "ok"));
        verify(reportMapper, never()).handle(any(), any(), any(), any(), any());
    }

    @Test
    void handle_whenVideoReportUpheld_shouldOfflineAndNotifyAuthor() {
        when(reportMapper.selectById(5L)).thenReturn(pendingReport(ReportTarget.TYPE_VIDEO, 3L));
        when(reportMapper.handle(any(), any(), any(), any(), any())).thenReturn(1);
        when(adminVideoMapper.updateStatus(3L, 1, 2)).thenReturn(1);
        Video video = new Video();
        video.setId(3L);
        video.setUserId(100L);
        when(adminVideoMapper.selectById(3L)).thenReturn(video);

        reportService.handle(1L, 5L, ReportTarget.STATUS_HANDLED, "违规下架");

        verify(adminVideoMapper).updateStatus(3L, 1, 2);
        verify(notificationService).notify(eq(100L), isNull(), eq(4), anyString(), anyString(), anyString(), eq(3L));
    }

    @Test
    void handle_whenCommentReportUpheld_shouldSoftDeleteComment() {
        when(reportMapper.selectById(5L)).thenReturn(pendingReport(ReportTarget.TYPE_COMMENT, 8L));
        when(reportMapper.handle(any(), any(), any(), any(), any())).thenReturn(1);
        Comment comment = new Comment();
        comment.setId(8L);
        comment.setUserId(42L);
        when(commentMapper.selectById(8L)).thenReturn(comment);

        reportService.handle(1L, 5L, ReportTarget.STATUS_HANDLED, null);

        // 复用评论软删：以评论作者身份触发 where 条件
        verify(commentMapper).softDelete(8L, 42L);
    }

    @Test
    void handle_whenRejected_shouldNotTouchTarget() {
        when(reportMapper.selectById(5L)).thenReturn(pendingReport(ReportTarget.TYPE_VIDEO, 3L));
        when(reportMapper.handle(any(), any(), any(), any(), any())).thenReturn(1);

        reportService.handle(1L, 5L, ReportTarget.STATUS_REJECTED, "证据不足");

        verify(adminVideoMapper, never()).updateStatus(anyLong(), anyInt(), anyInt());
    }
}
