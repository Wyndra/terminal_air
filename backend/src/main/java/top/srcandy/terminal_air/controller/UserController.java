package top.srcandy.terminal_air.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.pojo.vo.UserProfileVo;
import top.srcandy.terminal_air.request.UpdateProfileRequest;
import top.srcandy.terminal_air.service.AuthService;

@RestController
@Slf4j
@Validated
@RequestMapping("/api/user")
@Tag(name = "用户信息服务", description = "用户信息服务")
public class UserController {

    @Autowired
    private AuthService authService;


    @GetMapping("/avatar")
    @Operation(summary = "获取用户头像")
    public ResponseResult<String> getUserAvatar(@RequestHeader("Authorization") String token) {
        return authService.getUserAvatar(token.substring(7));
    }


    @GetMapping("/currentUser")
    @Operation(summary = "获取用户信息")
    public ResponseResult<UserProfileVo> currentUser() {
        return authService.getUserProfile();
    }


    @PutMapping("")
    @Operation(summary = "修改用户信息")
    public ResponseResult<UserProfileVo> updateProfile(@Valid @RequestBody(required = false) @NonNull UpdateProfileRequest request) {
        return authService.updateProfile(request);
    }
}
