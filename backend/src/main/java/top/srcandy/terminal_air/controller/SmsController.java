package top.srcandy.terminal_air.controller;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.pojo.vo.SmsCodeVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.request.SendVerificationCodeRequest;
import top.srcandy.terminal_air.request.VerifyCodeRequest;
import top.srcandy.terminal_air.service.SmsService;
import top.srcandy.terminal_air.aspectj.lang.annoations.AuthAccess;

@Slf4j
@RestController
@RequestMapping("/api/sms")
@Tag(name="SMS Service", description = "短信服务相关接口")
@OpenAPIDefinition
public class SmsController {
    @Autowired
    private SmsService smsService;

    @PostMapping ("/sendVerificationCode")
    @Operation(summary = "发送验证码")
    public ResponseResult<SmsCodeVO> sendVerifyCode(@Valid @RequestBody(required = false) @NotNull SendVerificationCodeRequest request) {
        try {
            return smsService.sendSmsCode(request);
        } catch (Exception e) {
            log.error("Failed to send SMS code", e);
        }
        return ResponseResult.success(null);
    }

    @PostMapping("/verifyCode")
    @Operation(summary = "验证验证码")
    public ResponseResult<Boolean> verifyCode(@Valid @RequestBody(required = false) @NotNull VerifyCodeRequest request) {
        return ResponseResult.success(smsService.verifySmsCode(request.getPhone(), request.getSerial(), request.getCode()));
    }

    @GetMapping("/sendSmsCodeByToken")
    @Operation(summary = "通过token发送验证码")
    public ResponseResult<SmsCodeVO> sendSmsCodeByToken() {
        try {
            return smsService.sendSmsCodeByToken();
        } catch (Exception e) {
            log.error("Failed to send SMS code", e);
        }
        return ResponseResult.success(null);
    }
}
