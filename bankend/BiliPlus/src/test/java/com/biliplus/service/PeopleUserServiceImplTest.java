package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.mapper.UserFollowMapper;
import com.biliplus.mapper.VideoTagMapper;
import com.biliplus.pojo.dto.userdto.UserRegisterDTO;
import com.biliplus.pojo.entity.User;
import com.biliplus.service.Impl.PeopleUserServiceImpl;
import com.biliplus.utils.LoginRateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleUserServiceImplTest {

    @Mock
    private PeopleUserMapper peopleUserMapper;

    @Mock
    private UserFollowMapper userFollowMapper;

    @Mock
    private VideoTagMapper videoTagMapper;

    @Mock
    private SensitiveWordService sensitiveWordService;

    @Mock
    private UserPenaltyService userPenaltyService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @InjectMocks
    private PeopleUserServiceImpl peopleUserService;

    private UserRegisterDTO dto() {
        UserRegisterDTO d = new UserRegisterDTO();
        d.setEmail("a@b.com");
        d.setPassword("pass1234");
        return d;
    }

    private User user(int status) {
        User u = new User();
        u.setId(9L);
        u.setEmail("a@b.com");
        u.setPassword("hash");
        u.setStatus(status);
        return u;
    }

    @Test
    void userlogin_whenStatusDisabled_shouldThrow() {
        when(loginRateLimiter.allowAccountLogin(anyString())).thenReturn(true);
        when(peopleUserMapper.userlogin("a@b.com")).thenReturn(user(0));
        when(passwordEncoder.matches("pass1234", "hash")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> peopleUserService.userlogin(dto()));
        assertTrue(ex.getMessage().contains("封禁") || ex.getMessage().contains("禁用"));
    }

    @Test
    void userlogin_whenBanned_shouldThrow() {
        when(loginRateLimiter.allowAccountLogin(anyString())).thenReturn(true);
        when(peopleUserMapper.userlogin("a@b.com")).thenReturn(user(1));
        when(passwordEncoder.matches("pass1234", "hash")).thenReturn(true);
        when(userPenaltyService.isBanned(9L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> peopleUserService.userlogin(dto()));
        assertTrue(ex.getMessage().contains("封禁"));
    }

    @Test
    void userlogin_whenOk_shouldReturnUser() {
        User u = user(1);
        when(loginRateLimiter.allowAccountLogin(anyString())).thenReturn(true);
        when(peopleUserMapper.userlogin("a@b.com")).thenReturn(u);
        when(passwordEncoder.matches("pass1234", "hash")).thenReturn(true);
        when(userPenaltyService.isBanned(9L)).thenReturn(false);

        assertSame(u, peopleUserService.userlogin(dto()));
    }
}
