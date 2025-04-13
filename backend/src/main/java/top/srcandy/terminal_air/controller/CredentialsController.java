package top.srcandy.terminal_air.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Null;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.pojo.vo.CredentialVO;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.pojo.model.Credential;
import top.srcandy.terminal_air.request.CredentialConnectionRequest;
import top.srcandy.terminal_air.request.GenerateKeyPairRequest;
import top.srcandy.terminal_air.request.CredentialStatusRequest;
import top.srcandy.terminal_air.service.CredentialsService;

import java.util.List;

@RestController
@Slf4j
@Validated
@Tag(name = "Credentials Service", description = "凭据接口")
@RequestMapping("/api/credentials")
public class CredentialsController {
    @Autowired
    private CredentialsService credentialsService;

    @PostMapping("/generate")
    @Operation(summary = "生成密钥对", description = "生成密钥对")
    public ResponseResult<CredentialVO> generateKeyPair(@RequestHeader("Authorization") String token, @RequestBody(required = false) @NonNull GenerateKeyPairRequest request) throws Exception {
        return ResponseResult.success(credentialsService.generateKeyPair(request.getName(), request.getTags()));
    }

    @GetMapping("/list")
    @Operation(summary = "凭据列表", description = "列出用户当前凭据")
    public ResponseResult<List<CredentialVO>> listCredentials(@RequestHeader("Authorization") String token) throws Exception {
        List<CredentialVO> credentials = credentialsService.listCredentials();
        return ResponseResult.success(credentials);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除凭据", description = "删除指定凭据")
    public ResponseResult<List<Credential>> deleteCredential(@PathVariable Long id) throws Exception {
        credentialsService.deleteCredential(id);
        return ResponseResult.success();
    }

    @PostMapping("/update/status")
    @Operation(summary = "更新凭据状态", description = "更新凭据状态 curl请求更新凭据状态")
    public ResponseResult<Null> updateCredentialStatus(@RequestBody CredentialStatusRequest request) throws Exception {
        credentialsService.updateCredentialStatus(request);
        return ResponseResult.success(null);
    }

    @PostMapping("/get/status/{uuid}")
    @Operation(summary = "获取凭据状态", description = "获取凭据状态")
    public ResponseResult<Integer> selectCredentialByUuid(@PathVariable String uuid) throws Exception {
        Integer status = credentialsService.selectCredentialByUuid(uuid).getStatus();
        return ResponseResult.success(status);
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定凭据", description = "绑定凭据")
    public ResponseResult<CredentialVO> updateCredentialConnectId(@RequestBody CredentialConnectionRequest request) throws Exception {
        return ResponseResult.success(credentialsService.updateCredentialConnectId(request));
    }

    @GetMapping("/get/bound/{uuid}")
    @Operation(summary = "获取当前Connection的已绑定凭据", description = "获取当前Connection的已绑定凭据")
    public ResponseResult<List<CredentialVO>> selectBoundCredentialsByConnectionId(@PathVariable String uuid) throws Exception {
        return ResponseResult.success(credentialsService.selectBoundCredentialsByConnectionId(uuid));
    }

    @GetMapping(value ="/installation/{uuid}", produces = "text/plain;charset=UTF-8")
    @Operation(summary = "安装脚本", description = "生成安装脚本")
    public String generateInstallShell(@RequestParam String token, @RequestParam String endpoint,@PathVariable String uuid) throws Exception {
        return credentialsService.generateInstallShell(token,uuid, endpoint);
    }

}
