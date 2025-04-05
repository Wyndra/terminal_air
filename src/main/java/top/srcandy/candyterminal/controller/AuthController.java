package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.aspectj.lang.annoations.PublicAccessValidate;
import top.srcandy.candyterminal.aspectj.lang.annoations.TwoFactorAuthRequired;
import top.srcandy.candyterminal.bean.vo.LoginResultVO;
import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.request.*;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.aspectj.lang.annoations.AuthAccess;
import top.srcandy.candyterminal.utils.JWTUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
@RestController
@Slf4j
@Validated
@RequestMapping("/api/auth")
@Tag(name = "Auth Service", description = "用户认证接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @AuthAccess
    @Operation(summary = "用户登录请求")
    public ResponseResult<LoginResultVO> login(@Valid @RequestBody(required = false) @NonNull LoginRequest request) {
//        return authService.login(request);
        // 请求转到新的登录方法，实现无感切换密码强度。
        return authService.loginAndChangePassword(request);
    }

    @PostMapping("/loginBySmsCode")
    @AuthAccess
    @Operation(summary = "用户短信验证码登录请求")
    public ResponseResult<String> loginByPhoneAndSmsCode(@Valid @RequestBody(required = false) @NonNull LoginBySmsCodeRequest request) {
        return authService.loginBySmsCode(request);
    }

    @PostMapping("/register")
    @AuthAccess
    @Operation(summary = "用户注册请求")
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


    @PostMapping("/loginRequireTwoFactorAuth")
    @TwoFactorAuthRequired
    public ResponseResult<String> loginRequireTwoFactorAuth(@Valid @RequestHeader("Authorization") String twoFactorAuthToken, @RequestBody(required = false) @NonNull VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return authService.loginRequireTwoFactorAuth(twoFactorAuthToken.substring(7),request);
    }

    @GetMapping("/getUserAvatar")
    @TwoFactorAuthRequired
    @PublicAccessValidate
    public ResponseResult<String> getUserAvatar(@RequestHeader("Authorization") String token) {
        return authService.getUserAvatar(token.substring(7));
    }

    @PostMapping("/updatePassword")
    public ResponseResult<String> updatePassword(@RequestHeader("Authorization") String token, @Valid @RequestBody(required = false) @NonNull UpdatePasswordRequest request) {
        return authService.updatePassword(token.substring(7), request);
    }
}
