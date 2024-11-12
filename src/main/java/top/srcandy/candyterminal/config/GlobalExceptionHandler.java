package top.srcandy.candyterminal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.exception.ServiceException;

import java.util.Objects;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ResponseResult<String> handleServiceException(ServiceException e) {
        // 返回一个json格式的错误信息
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
