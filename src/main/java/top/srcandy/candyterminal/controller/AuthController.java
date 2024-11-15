package top.srcandy.candyterminal.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.bean.vo.UserProfileVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.bean.dto.UserInfoDTO;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.LoginRequest;
import top.srcandy.candyterminal.request.RegisterRequest;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.utils.JWTUtil;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseResult<String> login(@Valid @RequestBody(required = false) @NonNull LoginRequest request) {
        return authService.login(User.builder().username(request.getUsername()).password(request.getPassword()).build());
    }

    @PostMapping("/register")
    public ResponseResult<String> register(@Valid @RequestBody(required = false) @NonNull RegisterRequest request) {
        log.info("register user:{}", request);
        return authService.register(User.builder().username(request.getUsername()).password(request.getPassword()).salt(request.getPassword()).build());
    }

    @GetMapping("/getProfile")
    public ResponseResult<UserProfileVO> getUserInfo(@RequestHeader("Authorization") String token) {
        return authService.getUserProfile(token.substring(7));
    }
//
//        @GetMapping("/updatePassword")
//        @ApiOperation("修改密码")
//        @ApiImplicitParam(name = "username", value = "用户名", required = true)
//        public String updatePassword(@RequestParam String username) {
//            return "update password success";
//        }
}
