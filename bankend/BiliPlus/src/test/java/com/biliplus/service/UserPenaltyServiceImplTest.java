package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.mapper.UserPenaltyMapper;
import com.biliplus.pojo.entity.UserPenalty;
import com.biliplus.result.PageResult;
import com.biliplus.service.Impl.UserPenaltyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPenaltyServiceImplTest {

    @Mock
    private UserPenaltyMapper userPenaltyMapper;

    @Mock
    private PeopleUserMapper peopleUserMapper;

    @Mock
    private EventLogService eventLogService;

    @Mock
    private AdminOperationLogService adminOperationLogService;

    @InjectMocks
    private UserPenaltyServiceImpl userPenaltyService;

    private UserPenalty active(long id, Long userId, String action) {
        UserPenalty p = new UserPenalty();
        p.setId(id);
        p.setUserId(userId);
        p.setAction(action);
        p.setStatus(1);
        p.setStartTime(LocalDateTime.now().minusMinutes(5));
        p.setEndTime(LocalDateTime.now().plusDays(7));
        return p;
    }

    @Test
    void penalize_whenAlreadyActive_shouldThrow() {
        when(userPenaltyMapper.countActive(9L, "mute")).thenReturn(1);
        assertThrows(BusinessException.class,
                () -> userPenaltyService.penalize(9L, "mute", "违规", 7, 1L));
        verify(userPenaltyMapper, never()).insert(any());
    }

    @Test
    void penalize_withDays_shouldSetEndTime() {
        when(userPenaltyMapper.countActive(9L, "mute")).thenReturn(0);

        userPenaltyService.penalize(9L, "mute", "违规发言", 7, 1L);

        ArgumentCaptor<UserPenalty> captor = ArgumentCaptor.forClass(UserPenalty.class);
        verify(userPenaltyMapper).insert(captor.capture());
        assertNotNull(captor.getValue().getEndTime());
        assertEquals(1, captor.getValue().getStatus());
    }

    @Test
    void penalize_whenPermanent_shouldSetNullEndTime() {
        when(userPenaltyMapper.countActive(9L, "ban")).thenReturn(0);

        userPenaltyService.penalize(9L, "ban", "严重违规", null, 1L);

        ArgumentCaptor<UserPenalty> captor = ArgumentCaptor.forClass(UserPenalty.class);
        verify(userPenaltyMapper).insert(captor.capture());
        assertNull(captor.getValue().getEndTime());
        // 封禁同步 user.status=0，使登录立即失效
        verify(peopleUserMapper).updateUserStatus(9L, 0);
    }

    @Test
    void penalize_whenMute_shouldNotTouchUserStatus() {
        when(userPenaltyMapper.countActive(9L, "mute")).thenReturn(0);

        userPenaltyService.penalize(9L, "mute", "违规", 7, 1L);

        verify(peopleUserMapper, never()).updateUserStatus(any(), any());
    }

    @Test
    void release_whenBan_shouldRestoreUserStatus() {
        when(userPenaltyMapper.releaseActive(9L, "ban")).thenReturn(1);

        userPenaltyService.release(9L, "ban", 1L);

        verify(peopleUserMapper).updateUserStatus(9L, 1);
    }

    @Test
    void release_whenNothingActive_shouldThrow() {
        when(userPenaltyMapper.releaseActive(9L, "mute")).thenReturn(0);
        assertThrows(BusinessException.class, () -> userPenaltyService.release(9L, "mute", 1L));
    }

    @Test
    void expireDue_shouldCloseAndRestoreBan() {
        UserPenalty ban = active(1L, 9L, "ban");
        when(userPenaltyMapper.selectExpired(any())).thenReturn(List.of(ban));
        when(userPenaltyMapper.changeStatus(1L, 2)).thenReturn(1);

        int count = userPenaltyService.expireDue();

        assertEquals(1, count);
        verify(peopleUserMapper).updateUserStatus(9L, 1);
    }

    @Test
    void expireDue_whenNone_shouldReturnZero() {
        when(userPenaltyMapper.selectExpired(any())).thenReturn(List.of());
        assertEquals(0, userPenaltyService.expireDue());
    }

    @Test
    void adminList_shouldPage() {
        when(userPenaltyMapper.adminList(null, 1, 0, 20)).thenReturn(List.of(active(1L, 9L, "mute")));
        when(userPenaltyMapper.adminCount(null, 1)).thenReturn(1L);

        PageResult result = userPenaltyService.adminList(null, 1, 1, 20);
        assertEquals(1L, result.getTotal());
    }
}
