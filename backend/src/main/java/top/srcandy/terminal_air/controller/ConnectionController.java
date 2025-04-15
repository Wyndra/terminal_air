package top.srcandy.terminal_air.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.pojo.vo.ConnectionVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.pojo.model.Connection;
import top.srcandy.terminal_air.request.AddConnectionRequest;
import top.srcandy.terminal_air.request.UpdateConnectionRequest;
import top.srcandy.terminal_air.service.ConnectionService;
import top.srcandy.terminal_air.utils.SecuritySessionUtils;

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
    public ResponseResult<List<ConnectionVO>> selectByConnectCreaterUid() {
        return connectManageService.list(SecuritySessionUtils.getUserId());
    }

    @PostMapping("/add")
    @Operation(summary = "添加连接")
    public ResponseResult<Connection> insertConnect(@RequestBody AddConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectManageService.insertConnect(request);
    }

    @PostMapping("/update")
    @Operation(summary = "更新连接")
    public ResponseResult<ConnectionVO> updateConnect(@RequestBody UpdateConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectManageService.updateConnect(request);
    }

    @PostMapping ("/delete/{uuid}")
    @Operation(summary = "删除连接")
    public ResponseResult<Connection> deleteConnect(@PathVariable String uuid) {
        return connectManageService.deleteConnect(uuid);
    }

}
