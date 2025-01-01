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
import top.srcandy.candyterminal.utils.SMSUtils;

@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    private SmsService smsService;

    @PostMapping ("/sendVerificationCode")
    public ResponseResult<SmsCodeVO> sendVerifyCode(@Valid @RequestBody(required = false) @NotNull SendVerificationCodeRequest request) {

        try {
            return smsService.sendSmsCode(request);
        } catch (Exception e) {
            log.error("Failed to send SMS code", e);
        }
        return ResponseResult.success(null);
    }

    @PostMapping("/verifyCode")
    public ResponseResult<Boolean> verifyCode(@Valid @RequestBody(required = false) @NotNull VerifyCodeRequest request) {
        return ResponseResult.success(smsService.verifySmsCode(request.getPhone(), request.getSerial(), request.getCode()));
    }
}
