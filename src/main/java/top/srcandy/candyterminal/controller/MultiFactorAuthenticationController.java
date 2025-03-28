package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.aspectj.lang.annoations.TwoFactorAuthRequired;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.request.VerifyTwoFactorAuthCodeRequest;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.MultiFactorAuthenticationService;
import top.srcandy.candyterminal.utils.JWTUtil;

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
    public ResponseResult<String> switchTwoFactorAuth(@RequestHeader("Authorization") String token) {
        return ResponseResult.success(multiFactorAuthenticationService.switchTwoFactorAuth(token.substring(7)));
    }

    @GetMapping("/getTwoFactorAuthSecretQRCode")
    @Operation(summary = "获取两步验证二维码")
    public ResponseResult<String> getTwoFactorAuthSecretQRCode(@RequestHeader("Authorization") String token) {
        return ResponseResult.success(multiFactorAuthenticationService.getTwoFactorAuthSecretQRCode(token.substring(7)));
    }

    @PostMapping("/verifyTwoFactorAuthCode")
    @TwoFactorAuthRequired
    @Operation(summary = "验证两步验证验证码")
    public ResponseResult<Boolean> verifyTwoFactorAuthCode(@RequestHeader("Authorization") String twoFactorAuthToken, @Valid @RequestBody(required = false) @NonNull VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return ResponseResult.success(multiFactorAuthenticationService.verifyTwoFactorAuthCode(twoFactorAuthToken.substring(7), request));
    }


    @GetMapping("/getTwoFactorAuthTokenByCurrentUser")
    @Operation(summary = "获取当前用户的两步验证Token")
    public ResponseResult<String> getTwoFactorAuthTokenByCurrentUser(@RequestHeader("Authorization") String token) {
        return ResponseResult.success(JWTUtil.generateTwoFactorAuthSecretToken(authService.getUserByUsername(JWTUtil.getTokenClaimMap(token.substring(7)).get("username").asString())));
    }
}
