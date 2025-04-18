package top.srcandy.terminal_air.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.exception.ServiceException;
import top.srcandy.terminal_air.exception.TooManyRequestsException;

import java.util.Objects;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ResponseResult<String> handleServiceException(ServiceException e) {
        // 返回一个json格式的错误信息
        log.error("ServiceException: {}", e.getMessage());
        return ResponseResult.fail(null, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseResult<String> handleBadCredentialsException(BadCredentialsException e) {
        log.error("登录异常: 错误信息={}", e.getMessage());
        return ResponseResult.fail(null, e.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseResult<String> handleTooManyRequestsException(TooManyRequestsException e) {
        log.error("验证码请求异常: 用户={}, 错误信息={}", e.getUser(), e.getMessage());
        return ResponseResult.fail(null, e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseResult<String> handleNullPointerException(NullPointerException e) {
        return ResponseResult.fail(null, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 得到校验的错误信息
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return ResponseResult.fail(null, message);
    }
}
