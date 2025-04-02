package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.bean.vo.ConnectionVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.Connection;
import top.srcandy.candyterminal.request.AddConnectionRequest;
import top.srcandy.candyterminal.request.DeleteConnectRequest;
import top.srcandy.candyterminal.request.UpdateConnectionRequest;
import top.srcandy.candyterminal.service.ConnectionService;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/connection")
@Tag(name = "Connection Service", description = "连接管理服务")
public class ConnectionController {
    @Autowired
    private ConnectionService connectManageService;
    @GetMapping("/list")
    @Operation(summary = "获取用户的连接列表")
    public ResponseResult<List<ConnectionVO>> selectByConnectCreaterUid(@RequestHeader("Authorization") String token) {
        return connectManageService.list(token);
    }

    @PostMapping("/add")
    @Operation(summary = "添加连接")
    public ResponseResult<Connection> insertConnect(@RequestHeader("Authorization") String token, @RequestBody AddConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectManageService.insertConnect(token, request);
    }

    @PostMapping("/update")
    @Operation(summary = "更新连接")
    public ResponseResult<ConnectionVO> updateConnect(@RequestHeader("Authorization") String token, @RequestBody UpdateConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectManageService.updateConnect(token, request);
    }

    @PostMapping ("/deleteConnect")
    @Operation(summary = "删除连接")
    public ResponseResult<Connection> deleteConnect(@RequestHeader("Authorization") String token, @RequestBody DeleteConnectRequest request) {
        return connectManageService.deleteConnect(token,request.getCid());
    }

}
