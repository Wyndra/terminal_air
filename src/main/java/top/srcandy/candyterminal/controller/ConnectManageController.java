package top.srcandy.candyterminal.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.bean.dto.AddNewConnectDTO;
import top.srcandy.candyterminal.bean.dto.DeleteConnectDTO;
import top.srcandy.candyterminal.model.ConnectInfo;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.AddConnectRequest;
import top.srcandy.candyterminal.request.DeleteConnectRequest;
import top.srcandy.candyterminal.request.UpdateConnectRequest;
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
        return connectManageService.getUserConnects(token);
    }

    @PostMapping("/addConnect")
    public ResponseResult<ConnectInfo> insertConnect(@RequestHeader("Authorization") String token, @RequestBody AddConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectManageService.insertConnect(token, request);
    }

    @PostMapping("/updateConnect")
    public ResponseResult<ConnectInfo> updateConnect(@RequestHeader("Authorization") String token, @RequestBody UpdateConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
//        String token_no_bearer = token.substring(7);
//        String username = JWTUtil.getTokenClaimMap(token_no_bearer).get("username").asString();
//        User user = authService.getUserByUsername(username);
//        if (user == null) {
//            return ResponseResult.fail(null, "用户不存在");
//        }
//        return connectManageService.updateConnect();
        return connectManageService.updateConnect(token, request);
    }

    @PostMapping ("/deleteConnect")
    public ResponseResult<ConnectInfo> deleteConnect(@RequestHeader("Authorization") String token, @RequestBody DeleteConnectRequest request) {
        return connectManageService.deleteConnect(token,request.getCid());
    }

}
