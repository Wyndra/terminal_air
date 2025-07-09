package top.srcandy.terminal_air.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.pojo.vo.ConnectionVo;
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
@Tag(name = "连接信息服务", description = "连接信息服务")
public class ConnectionController {

    @Autowired
    private ConnectionService connectService;

    @GetMapping("")
    @Operation(summary = "获取连接列表")
    public ResponseResult<List<ConnectionVo>> selectByConnectCreaterUid() {
        return connectService.list(SecuritySessionUtils.getUserId());
    }

    @PostMapping("")
    @Operation(summary = "添加连接")
    public ResponseResult<ConnectionVo> insertConnect(@ModelAttribute  AddConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectService.insertConnect(request);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "更新连接")
    public ResponseResult<ConnectionVo> updateConnect(@ModelAttribute  UpdateConnectionRequest request) throws GeneralSecurityException, UnsupportedEncodingException {
        return connectService.updateConnect(request);
    }

    @DeleteMapping ("/{uuid}")
    @Operation(summary = "删除连接")
    public ResponseResult<Connection> deleteConnect(@PathVariable String uuid) {
        return connectService.deleteConnect(uuid);
    }

}
