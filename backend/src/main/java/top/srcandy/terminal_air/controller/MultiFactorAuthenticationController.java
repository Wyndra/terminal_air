package top.srcandy.terminal_air.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Null;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.mapper.TwoFactorAuthMapper;
import top.srcandy.terminal_air.pojo.vo.EnableTwoFactorAuthVo;
import top.srcandy.terminal_air.request.EnableTwoFactorAuthCodeRequest;
import top.srcandy.terminal_air.request.VerifyOneTimeBackupCodeRequest;
import top.srcandy.terminal_air.request.VerifyTwoFactorAuthCodeRequest;
import top.srcandy.terminal_air.service.AuthService;
import top.srcandy.terminal_air.service.MultiFactorAuthenticationService;
import top.srcandy.terminal_air.utils.JWTUtil;
import top.srcandy.terminal_air.utils.SecuritySessionUtils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

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

    @Autowired
    private TwoFactorAuthMapper twoFactorAuthMapper;

    @GetMapping("/switchTwoFactorAuth")
    @Operation(summary = "关闭/开启两步验证")
    public ResponseResult<Boolean> switchTwoFactorAuth() {
        return ResponseResult.success(multiFactorAuthenticationService.switchTwoFactorAuth());
    }

    @GetMapping("/twoFactorAuth/init")
    @Operation(summary = "初始化用户两步验证")
    public ResponseResult<String> initTwoFactorAuth() {
        return ResponseResult.success(multiFactorAuthenticationService.initTwoFactorAuth());
    }

    @PostMapping("/twoFactorAuth/enable")
    @Operation(summary = "开启两步验证")
    public ResponseResult<EnableTwoFactorAuthVo> enableTwoFactorAuth(@RequestBody(required = false) EnableTwoFactorAuthCodeRequest request){
        return ResponseResult.success(multiFactorAuthenticationService.enableTwoFactorAuth(request));
    }

    @GetMapping("/twoFactorAuth/backup")
    @Operation(summary = "下载一次性验证码备份")
    public ResponseEntity<Resource> downloadOneTimeCodeBackup() {
        return multiFactorAuthenticationService.downloadOneTimeCodeBackup();
    }

    @GetMapping("/twoFactorAuth/status")
    @Operation(summary = "获取当前用户两步验证状态")
    public ResponseResult<Boolean> getCurrentTwoFactorAuthStatus(){
        return ResponseResult.success(multiFactorAuthenticationService.getCurrentTwoFactorAuthStatus());
    }

    @GetMapping("/getTwoFactorAuthSecretQRCode")
    @Operation(summary = "获取两步验证二维码")
    public ResponseResult<String> getTwoFactorAuthSecretQRCode() {
        return ResponseResult.success(multiFactorAuthenticationService.getTwoFactorAuthSecretQRCode());
    }

    @PostMapping("/verifyTwoFactorAuthCode")
    @Operation(summary = "验证两步验证验证码")
    public ResponseResult<Boolean> verifyTwoFactorAuthCode(@RequestBody(required = false) VerifyTwoFactorAuthCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return ResponseResult.success(multiFactorAuthenticationService.verifyTwoFactorAuthCode(request));
    }

    @PostMapping("/verifyOneTimeBackupCode")
    @Operation(summary = "验证一次性备份验证码")
    public ResponseResult<Boolean> verifyOneTimeBackupCode(@RequestBody(required = false) VerifyOneTimeBackupCodeRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return ResponseResult.success(multiFactorAuthenticationService.verifyOneTimeBackupCode(request));
    }

    @GetMapping("/getTwoFactorAuthTitle")
    @Operation(summary = "获取身份验证器的名称")
    public ResponseResult<String> getTwoFactorAuthTitle() {
        return ResponseResult.success(multiFactorAuthenticationService.getTwoFactorAuthTitle());
    }

    @GetMapping("/getTwoFactorAuthTokenByCurrentUser")
    @Operation(summary = "获取当前用户的两步验证Token")
    @Deprecated
    public ResponseResult<String> getTwoFactorAuthTokenByCurrentUser(@RequestHeader("Authorization") String token) {
        String username = SecuritySessionUtils.getUsername();
        return ResponseResult.success(JWTUtil.generateTwoFactorAuthSecretToken(authService.getUserByUsername(username),twoFactorAuthMapper.getUserTwoFactorAuthSecret(SecuritySessionUtils.getUserId())));
    }

    @GetMapping("/refreshOneTimeCodeBackup")
    @Operation(summary = "刷新一次性备份验证码")
    public ResponseResult<ArrayList<String>> refreshOneTimeCodeBackup() throws GeneralSecurityException, UnsupportedEncodingException {
        return ResponseResult.success(multiFactorAuthenticationService.refreshOneTimeCodeBackupList());
    }

    @GetMapping("/disableTwoFactorAuth")
    @Operation(summary = "关闭两步验证")
    public ResponseResult<Void> disableTwoFactorAuth() {
        multiFactorAuthenticationService.disableTwoFactorAuth();
        return ResponseResult.success();
    }



}
