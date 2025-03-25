package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.ConnectInfo;
import top.srcandy.candyterminal.request.AddConnectRequest;
import top.srcandy.candyterminal.request.DeleteConnectRequest;
import top.srcandy.candyterminal.request.UpdateConnectRequest;
import top.srcandy.candyterminal.service.ConnectManageService;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/connect")
public class ConnectManageController {
    @Autowired
    private ConnectManageService connectManageService;
    @GetMapping("/list")
    @Operation(summary = "获取用户的连接列表")
    public ResponseResult<List<ConnectInfo>> selectByConnectCreaterUid(@RequestHeader("Authorization") String token) {
        return connectManageService.list(token);
    }

    @PostMapping("/add")
    public ResponseResult<ConnectInfo> insertConnect(@RequestHeader("Authorization") String token, @RequestBody AddConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectManageService.insertConnect(token, request);
    }

    @PostMapping("/update")
    public ResponseResult<ConnectInfo> updateConnect(@RequestHeader("Authorization") String token, @RequestBody UpdateConnectRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectManageService.updateConnect(token, request);
    }

    @PostMapping ("/deleteConnect")
    public ResponseResult<ConnectInfo> deleteConnect(@RequestHeader("Authorization") String token, @RequestBody DeleteConnectRequest request) {
        return connectManageService.deleteConnect(token,request.getCid());
    }

}
