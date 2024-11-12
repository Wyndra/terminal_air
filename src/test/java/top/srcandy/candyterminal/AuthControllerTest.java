package top.srcandy.candyterminal;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.constant.ResponseStatus;
import top.srcandy.candyterminal.controller.AuthController;
import top.srcandy.candyterminal.dto.LoginDTO;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController yourController;

    @Test
    public void testLogin() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("");
        loginDTO.setPassword("");

        log.info("loginDTO:{}", loginDTO);
        // Mock the behavior of authService
        when(authService.login(any(User.class))).thenReturn(ResponseResult.success("token123"));
        // Act
        ResponseResult<String> response = yourController.login(loginDTO);
        log.info("response:{}", response);


        // Assert
//        assertEquals(ResponseStatus.SUCCESS, response.getStatus());
//        assertEquals("success", response.getMessage());
        assertEquals("token123", response.getData());
    }
}
