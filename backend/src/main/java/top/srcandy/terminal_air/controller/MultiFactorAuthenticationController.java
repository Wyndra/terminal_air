package top.srcandy.terminal_air.controller;

import cn.hutool.core.lang.copier.SrcToDestCopier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.aspectj.lang.annoations.TwoFactorAuthRequired;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.request.VerifyTwoFactorAuthCodeRequest;
import top.srcandy.terminal_air.service.AuthService;
import top.srcandy.terminal_air.service.MultiFactorAuthenticationService;
import top.srcandy.terminal_air.utils.JWTUtil;
import top.srcandy.terminal_air.utils.SecurityUtils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

@RestController
@Slf4j
@Validated
@RequestMapping("/api/mfa")
@Tag(name = "Multi Factor Authentication Service", description = "多因素认证接口")
public class MultiFactorAuthenticationController {

    @Autowired
    private MultiFactorAuthenticationService multiFactorAuthenticationService;

    @Autowired
    private AuthService authService;

    @GetMapping("/switchTwoFactorAuth")
    @Operation(summary = "关闭/开启两步验证")
    public ResponseResult<String> switchTwoFactorAuth() {
        return ResponseResult.success(multiFactorAuthenticationService.switchTwoFactorAuth());
    }

    @GetMapping("/getTwoFactorAuthSecretQRCode")
    @Operation(summary = "获取两步验证二维码")
    public ResponseResult<String> getTwoFactorAuthSecretQRCode() {
        return ResponseResult.success(multiFactorAuthenticationService.getTwoFactorAuthSecretQRCode());
    }

    @PostMapping("/verifyTwoFactorAuthCode")
    @Operation(summary = "验证两步验证验证码")
    public ResponseResult<Boolean> verifyTwoFactorAuthCode(@RequestHeader("Authorization") String twoFactorAuthToken, @RequestBody(required = false) VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return ResponseResult.success(multiFactorAuthenticationService.verifyTwoFactorAuthCode(twoFactorAuthToken.substring(7), request));
    }


    @GetMapping("/getTwoFactorAuthTokenByCurrentUser")
    @Operation(summary = "获取当前用户的两步验证Token")
    public ResponseResult<String> getTwoFactorAuthTokenByCurrentUser(@RequestHeader("Authorization") String token) {
        String username = SecurityUtils.getUsername();
        return ResponseResult.success(JWTUtil.generateTwoFactorAuthSecretToken(authService.getUserByUsername(username)));
    }
}
