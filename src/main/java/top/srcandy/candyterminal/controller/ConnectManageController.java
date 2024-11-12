package top.srcandy.candyterminal.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.dto.AddNewConnectDTO;
import top.srcandy.candyterminal.dto.DeleteConnectDTO;
import top.srcandy.candyterminal.model.ConnectInfo;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.ConnectManageService;
import top.srcandy.candyterminal.utils.AESUtils;
import top.srcandy.candyterminal.utils.JWTUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/connect")
public class ConnectManageController {
    @Autowired
    private ConnectManageService connectManageService;

    @Autowired
    private AuthService authService;

    @GetMapping("/getConnect")
    public ResponseResult<List<ConnectInfo>> selectByConnectCreaterUid(@RequestHeader("Authorization") String token) {
        String token_no_bearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(token_no_bearer).get("username").asString();
        log.info("username:{}", username);
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        log.info("user:{}", user);
        return connectManageService.selectByConnectCreaterUid(user.getUid());
    }

    @PostMapping("/addConnect")
    public ResponseResult<ConnectInfo> insertConnect(@RequestHeader("Authorization") String token, @RequestBody AddNewConnectDTO connect) throws GeneralSecurityException, UnsupportedEncodingException {
        String token_no_bearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(token_no_bearer).get("username").asString();
        User user = authService.getUserByUsername(username);
        // 得到用户加密密钥
        String salt = authService.getSaltByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        List<ConnectInfo> user_connects = connectManageService.selectByConnectCreaterUid(user.getUid()).getData();
        log.info("user_connects:{}", user_connects);
        // 判断新增的连接是否已经存在
        for (ConnectInfo user_connect : user_connects) {
            if (user_connect.getConnectHost().equals(connect.getHost())){
                return ResponseResult.fail(null, "连接已存在");
            }
        }

        ConnectInfo connect_to_insert = ConnectInfo.builder()
                .connect_creater_uid(user.getUid())
                .connectHost(connect.getHost())
                .connectPort(connect.getPort())
                .connectUsername(connect.getUsername())
                .connectMethod(connect.getMethod())
                .connectPwd(AESUtils.encryptToHex(connect.getPassword(), salt))
                .connectName(connect.getName())
                .build();
        return connectManageService.insertConnect(connect_to_insert);
    }

    @PostMapping("/updateConnect")
    public ResponseResult<ConnectInfo> updateConnect(@RequestHeader("Authorization") String token, @RequestBody ConnectInfo connect) {
        String token_no_bearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(token_no_bearer).get("username").asString();
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        return connectManageService.updateConnect(connect);
    }

    @PostMapping ("/deleteConnect")
    public ResponseResult<ConnectInfo> deleteConnect(@RequestHeader("Authorization") String token, @RequestBody DeleteConnectDTO deleteConnectDTO) {
        String token_no_bearer = token.substring(7);
        String username = JWTUtil.getTokenClaimMap(token_no_bearer).get("username").asString();
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return ResponseResult.fail(null, "用户不存在");
        }
        return connectManageService.deleteConnect(deleteConnectDTO.getCid());
    }

}
