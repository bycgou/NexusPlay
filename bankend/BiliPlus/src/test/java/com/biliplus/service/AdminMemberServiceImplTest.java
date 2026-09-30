package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.AdminMemberMapper;
import com.biliplus.pojo.vo.AdminMemberVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.Impl.AdminMemberServiceImpl;
import com.biliplus.service.Impl.UserPenaltyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceImplTest {

    @Mock
    private AdminMemberMapper adminMemberMapper;

    @Mock
    private UserPenaltyService userPenaltyService;

    @Mock
    private AdminOperationLogService adminOperationLogService;

    @InjectMocks
    private AdminMemberServiceImpl adminMemberService;

    @Test
    void list_shouldPassNumericKeywordAsId() {
        AdminMemberVO vo = new AdminMemberVO();
        vo.setId(12L);
        when(adminMemberMapper.adminList("12", 12L, 1, 0, 0, 20)).thenReturn(List.of(vo));
        when(adminMemberMapper.adminCount("12", 12L, 1, 0)).thenReturn(1L);

        PageResult result = adminMemberService.list("12", 1, 0, 1, 20);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void list_shouldTreatNonNumericKeywordAsNullId() {
        when(adminMemberMapper.adminList(eq("小明"), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of());
        when(adminMemberMapper.adminCount(eq("小明"), isNull(), isNull(), isNull())).thenReturn(0L);

        PageResult result = adminMemberService.list("小明", null, null, 1, 20);

        assertEquals(0L, result.getTotal());
    }

    @Test
    void detail_whenMissing_shouldThrow() {
        when(adminMemberMapper.selectDetail(9L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> adminMemberService.detail(9L));
    }

    @Test
    void ban_shouldDelegateToPenaltyService() {
        AdminMemberVO exist = new AdminMemberVO();
        exist.setId(5L);
        when(adminMemberMapper.selectDetail(5L)).thenReturn(exist);

        adminMemberService.ban(5L, "违规", 7, 1L);

        verify(userPenaltyService).penalize(5L, UserPenaltyServiceImpl.ACTION_BAN, "违规", 7, 1L);
    }

    @Test
    void updateRole_whenInvalid_shouldThrow() {
        AdminMemberVO exist = new AdminMemberVO();
        exist.setId(5L);
        when(adminMemberMapper.selectDetail(5L)).thenReturn(exist);

        assertThrows(BusinessException.class, () -> adminMemberService.updateRole(5L, 9));
        verify(adminMemberMapper, never()).updateRole(any(), any());
    }
}
