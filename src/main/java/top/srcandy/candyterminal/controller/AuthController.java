package top.srcandy.candyterminal.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.bean.vo.LoginResultVO;
import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.request.*;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.utils.AuthAccess;
import top.srcandy.candyterminal.utils.JWTUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

@RestController
@Slf4j
@Validated
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @AuthAccess
    public ResponseResult<LoginResultVO> login(@Valid @RequestBody(required = false) @NonNull LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/loginBySmsCode")
    @AuthAccess
    public ResponseResult<String> loginByPhoneAndSmsCode(@Valid @RequestBody(required = false) @NonNull LoginBySmsCodeRequest request) {
        return authService.loginBySmsCode(request);
    }

    @PostMapping("/register")
    @AuthAccess
    public ResponseResult<String> register(@Valid @RequestBody(required = false) @NonNull RegisterRequest request) {
        log.info("register user:{}", request);
        return authService.register(request);
    }

    @GetMapping("/getProfile")
    public ResponseResult<UserProfileVO> getUserInfo(@RequestHeader("Authorization") String token) {
        return authService.getUserProfile(token.substring(7));
    }

    @GetMapping("/getSalt")
    public ResponseResult<String> getSaltByUsername(@RequestHeader("Authorization") String token) {
        return ResponseResult.success(authService.getSaltByUsername(JWTUtil.getTokenClaimMap(token.substring(7)).get("username").asString()));
    }

    @PostMapping("/verifyUserPassword")
    public ResponseResult<Boolean> verifyUserPassword(@RequestHeader("Authorization") String token, @RequestBody(required = false) @NonNull VerifyUserPasswordRequest request) {
        log.info("verifyUserPassword:{}", request.getPassword());
        return ResponseResult.success(authService.verifyUserPassword(token.substring(7), request.getPassword()));
    }

    @PostMapping("/updateProfile")
    public ResponseResult<UserProfileVO> updateProfile(@RequestHeader("Authorization") String token, @Valid @RequestBody(required = false) @NonNull UpdateProfileRequest request) {
        return authService.updateProfile(token.substring(7), request);
    }

    @GetMapping("/switchTwoFactorAuth")
    public ResponseResult<String> switchTwoFactorAuth(@RequestHeader("Authorization") String token) {
        return ResponseResult.success(authService.switchTwoFactorAuth(token.substring(7)));
    }

    @GetMapping("/getTwoFactorAuthSecretQRCode")
    public ResponseResult<String> getTwoFactorAuthSecretQRCode(@RequestHeader("Authorization") String token) {
        return ResponseResult.success(authService.getTwoFactorAuthSecretQRCode(token.substring(7)));
    }

    @PostMapping("/verifyTwoFactorAuthCode")
    public ResponseResult<Boolean> verifyTwoFactorAuthCode(@RequestHeader("Authorization") String twoFactorAuthToken, @Valid @RequestBody(required = false) @NonNull VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return ResponseResult.success(authService.verifyTwoFactorAuthCode(twoFactorAuthToken.substring(7), request));
    }

    @GetMapping("/getTwoFactorAuthTokenByCurrentUser")
    public ResponseResult<String> getTwoFactorAuthTokenByCurrentUser(@RequestHeader("Authorization") String token) {
        return ResponseResult.success(JWTUtil.generateTwoFactorAuthSecretToken(authService.getUserByUsername(JWTUtil.getTokenClaimMap(token.substring(7)).get("username").asString())));
    }

    @PostMapping("/loginRequireTwoFactorAuth")
    public ResponseResult<String> loginRequireTwoFactorAuth(@Valid @RequestHeader("Authorization") String twoFactorAuthToken, @RequestBody(required = false) @NonNull VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return authService.loginRequireTwoFactorAuth(twoFactorAuthToken.substring(7),request);
    }
}
