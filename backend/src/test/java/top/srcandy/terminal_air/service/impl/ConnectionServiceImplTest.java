package top.srcandy.terminal_air.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import top.srcandy.terminal_air.mapper.UserMapper;
import top.srcandy.terminal_air.pojo.LoginUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ConnectionServiceImplTest {

    @Autowired
    private ConnectionServiceImpl connectionService;

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
    void list2() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional.ofNullable(authentication).orElseThrow(() -> new RuntimeException("未登录,请先登录"));
        LoginUser user = (LoginUser) authentication.getPrincipal();
        log.info(connectionService.list2(user.getUserId()).toString());
    }
}