package top.srcandy.candyterminal.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.bean.vo.SmsCodeVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.request.SendVerificationCodeRequest;
import top.srcandy.candyterminal.request.VerifyCodeRequest;
import top.srcandy.candyterminal.service.SmsService;
import top.srcandy.candyterminal.utils.AuthAccess;
import top.srcandy.candyterminal.utils.SMSUtils;

@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    private SmsService smsService;

    @PostMapping ("/sendVerificationCode")
    @AuthAccess
    public ResponseResult<SmsCodeVO> sendVerifyCode(@Valid @RequestBody(required = false) @NotNull SendVerificationCodeRequest request) {

        try {
            return smsService.sendSmsCode(request);
        } catch (Exception e) {
            log.error("Failed to send SMS code", e);
        }
        return ResponseResult.success(null);
    }

    @PostMapping("/verifyCode")
    @AuthAccess
    public ResponseResult<Boolean> verifyCode(@Valid @RequestBody(required = false) @NotNull VerifyCodeRequest request) {
        return ResponseResult.success(smsService.verifySmsCode(request.getPhone(), request.getSerial(), request.getCode()));
    }

    @GetMapping("/sendSmsCodeByToken")
    public ResponseResult<SmsCodeVO> sendSmsCodeByToken(@RequestHeader("Authorization") String token) {
        String token_no_bearer = token.substring(7);
        try {
            return smsService.sendSmsCodeByToken(token_no_bearer);
        } catch (Exception e) {
            log.error("Failed to send SMS code", e);
        }
        return ResponseResult.success(null);
    }
}
