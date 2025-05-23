package top.srcandy.terminal_air.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.pojo.vo.LoginResultVo;
import top.srcandy.terminal_air.pojo.vo.UserProfileVo;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.request.*;
import top.srcandy.terminal_air.service.AuthService;
import top.srcandy.terminal_air.utils.JWTUtil;

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
    @Operation(summary = "用户登录")
    public ResponseResult<LoginResultVo> login(@Valid @RequestBody(required = false) @NonNull LoginRequest request) {
        log.info("login user:{}", request);
        return authService.loginSecurity(request);
    }

    @PostMapping("/loginBySmsCode")
    @Operation(summary = "用户短信验证码登录")
    public ResponseResult<String> loginByPhoneAndSmsCode(@Valid @RequestBody(required = false) @NonNull LoginBySmsCodeRequest request) {
        return authService.loginSecurityBySmsCode(request);
    }

    @PostMapping("/loginRequireTwoFactorAuth")
    @Operation(summary = "用户二次认证的登录")
    public ResponseResult<String> loginRequireTwoFactorAuth(@Valid @RequestHeader("Authorization") String twoFactorAuthToken, @RequestBody(required = false) @NonNull VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return authService.loginSecurityRequireTwoFactorAuth(twoFactorAuthToken.substring(7), request);
    }


    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ResponseResult<String> register(@Valid @RequestBody(required = false) @NonNull RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/logout")
    @Operation(summary = "用户登出")
    public ResponseResult<String> logout() {
        authService.logout();
        return ResponseResult.success("登出成功");
    }

    @GetMapping("/getProfile")
    @Operation(summary = "获取用户信息")
    public ResponseResult<UserProfileVo> getUserInfo() {
        return authService.getUserProfile();
    }

    @GetMapping("/getSalt")
    @Operation(summary = "获取用户盐值")
    public ResponseResult<String> getSaltByUsername(@RequestHeader("Authorization") String token) {
        return ResponseResult.success(authService.getSaltByUsername(JWTUtil.getTokenClaimMap(token.substring(7)).get("username").asString()));
    }

    @PostMapping("/verifyUserPassword")
    @Operation(summary = "验证用户密码")
    public ResponseResult<Boolean> verifyUserPassword(@RequestBody(required = false) @NonNull VerifyUserPasswordRequest request) {
        log.info("verifyUserPassword:{}", request.getPassword());
        return ResponseResult.success(authService.verifyUserPassword(request.getPassword()));
    }

    @PostMapping("/updateProfile")
    @Operation(summary = "修改用户信息")
    public ResponseResult<UserProfileVo> updateProfile(@Valid @RequestBody(required = false) @NonNull UpdateProfileRequest request) {
        return authService.updateProfile(request);
    }


    @GetMapping("/getUserAvatar")
    @Operation(summary = "获取用户头像")
    public ResponseResult<String> getUserAvatar(@RequestHeader("Authorization") String token) {
        return authService.getUserAvatar(token.substring(7));
    }

    @PostMapping("/updatePassword")
    @Operation(summary = "修改用户密码")
    public ResponseResult<String> updatePassword(@Valid @RequestBody(required = false) @NonNull UpdatePasswordRequest request) {
        return authService.updatePassword(request);
    }
}
