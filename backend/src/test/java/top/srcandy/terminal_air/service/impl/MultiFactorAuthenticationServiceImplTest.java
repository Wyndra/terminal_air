package top.srcandy.terminal_air.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.pojo.LoginUser;
import top.srcandy.terminal_air.request.VerifyOneTimeBackupCodeRequest;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class MultiFactorAuthenticationServiceImplTest {

    @Autowired
    private MultiFactorAuthenticationServiceImpl multiFactorAuthenticationServiceImpl;

    @Resource
    private UserMapper userMapper;

    @BeforeEach
    void setupAuthentication() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(userMapper.selectByUserName("wyndra"));
        loginUser.setUserId(24L);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testCodeBackupUse() throws Exception {
        VerifyOneTimeBackupCodeRequest request = mock(VerifyOneTimeBackupCodeRequest.class);
        when(request.getCode()).thenReturn("1234-2225");
        assertTrue(multiFactorAuthenticationServiceImpl.verifyOneTimeBackupCode(request));
    }

}