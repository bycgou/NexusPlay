package com.biliplus.service;

import com.biliplus.mapper.UserCreditMapper;
import com.biliplus.pojo.entity.UserCredit;
import com.biliplus.service.Impl.UserCreditServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreditServiceImplTest {

    @Mock
    private UserCreditMapper userCreditMapper;

    @Mock
    private UserPenaltyService userPenaltyService;

    @InjectMocks
    private UserCreditServiceImpl userCreditService;

    private UserCredit credit(int score) {
        UserCredit c = new UserCredit();
        c.setUserId(9L);
        c.setScore(score);
        c.setViolationCount(1);
        return c;
    }

    @Test
    void applyViolation_shouldAccumulateViolation() {
        when(userCreditMapper.selectByUserId(9L)).thenReturn(credit(95));

        userCreditService.applyViolation(9L, -10, "举报成立");

        verify(userCreditMapper).ensure(9L);
        verify(userCreditMapper).changeScore(9L, -10, 1);
    }

    @Test
    void applyViolation_whenBelowShortThreshold_shouldMuteSevenDays() {
        when(userCreditMapper.selectByUserId(9L)).thenReturn(credit(55));

        userCreditService.applyViolation(9L, -5, "命中敏感词");

        verify(userPenaltyService).penalize(eq(9L), eq("mute"), anyString(), eq(7), isNull());
    }

    @Test
    void applyViolation_whenBelowLongThreshold_shouldMuteThirtyDays() {
        when(userCreditMapper.selectByUserId(9L)).thenReturn(credit(25));

        userCreditService.applyViolation(9L, -10, "稿件被下架");

        verify(userPenaltyService).penalize(eq(9L), eq("mute"), anyString(), eq(30), isNull());
    }

    @Test
    void applyViolation_whenPenaltyFails_shouldNotPropagate() {
        when(userCreditMapper.selectByUserId(9L)).thenReturn(credit(25));
        doThrow(new RuntimeException("downstream")).when(userPenaltyService)
                .penalize(any(), any(), any(), any(), any());

        // 自动禁言失败不能影响扣分主流程
        assertDoesNotThrow(() -> userCreditService.applyViolation(9L, -10, "稿件被下架"));
    }

    @Test
    void applyReward_shouldIncreaseWithoutViolation() {
        userCreditService.applyReward(9L, 2);

        verify(userCreditMapper).changeScore(9L, 2, 0);
        verify(userPenaltyService, never()).penalize(any(), any(), any(), any(), any());
    }

    @Test
    void applyViolation_whenUserIdNull_shouldSkip() {
        userCreditService.applyViolation(null, -10, "x");
        verify(userCreditMapper, never()).changeScore(any(), anyInt(), anyInt());
    }
}
