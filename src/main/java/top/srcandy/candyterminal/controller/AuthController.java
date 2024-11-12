package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.dto.LoginDTO;
import top.srcandy.candyterminal.dto.RegisterDTO;
import top.srcandy.candyterminal.dto.UserInfoDTO;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.utils.JWTUtil;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseResult<String> login(@Valid @RequestBody(required = false) @NonNull LoginDTO userdto) {
        return authService.login(User.builder().username(userdto.getUsername()).password(userdto.getPassword()).build());
    }

    @PostMapping("/register")
    public ResponseResult<String> register(@Valid @RequestBody(required = false) @NonNull RegisterDTO userdto) {
        log.info("register user:{}", userdto);
        return authService.register(User.builder().username(userdto.getUsername()).password(userdto.getPassword()).build());
    }

    @GetMapping("/getUserInfo")
    public ResponseResult<UserInfoDTO> getUserInfo(@RequestHeader("Authorization") String token) {
        String token_no_bearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(token_no_bearer).get("username").asString();
        log.info("username:{}", username);
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        UserInfoDTO userInfoDTO = UserInfoDTO.builder().username(user.getUsername()).email(user.getEmail()).nickname(user.getNickname()).build();
        return ResponseResult.success(userInfoDTO);
    }
//
//        @GetMapping("/updatePassword")
//        @ApiOperation("修改密码")
//        @ApiImplicitParam(name = "username", value = "用户名", required = true)
//        public String updatePassword(@RequestParam String username) {
//            return "update password success";
//        }
}
