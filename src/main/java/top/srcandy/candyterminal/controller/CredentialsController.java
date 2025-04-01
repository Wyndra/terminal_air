package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.aspectj.lang.annoations.AuthAccess;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.request.CredentialConnectionRequest;
import top.srcandy.candyterminal.request.GenerateKeyPairRequest;
import top.srcandy.candyterminal.request.CredentialStatusRequest;
import top.srcandy.candyterminal.service.CredentialsService;

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
    public ResponseResult<Credential> generateKeyPair(@RequestHeader("Authorization") String token, @RequestBody(required = false) @NonNull GenerateKeyPairRequest request) throws Exception {
        return ResponseResult.success(credentialsService.generateKeyPair(token.substring(7), request.getName(), request.getTags()));
    }

    @GetMapping("/list")
    @Operation(summary = "凭据列表", description = "列出用户当前凭据")
    public ResponseResult<List<Credential>> listCredentials(@RequestHeader("Authorization") String token) throws Exception {
        List<Credential> credentials = credentialsService.listCredentials(token.substring(7));
        return ResponseResult.success(credentials);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除凭据", description = "删除指定凭据")
    public ResponseResult<List<Credential>> deleteCredential(@RequestHeader("Authorization") String token, @PathVariable Long id) throws Exception {
        credentialsService.deleteCredential(token.substring(7), id);
        return ResponseResult.success();
    }

    @PostMapping("/update/status")
    @Operation(summary = "更新凭据状态", description = "更新凭据状态 curl请求更新凭据状态")
    public ResponseResult<List<Credential>> updateCredentialStatus(@RequestHeader("Authorization") String token, @RequestBody CredentialStatusRequest request) throws Exception {
        credentialsService.updateCredentialStatus(token.substring(7), request);
        return ResponseResult.success(null);
    }

    @PostMapping("/get/status/{uuid}")
    @Operation(summary = "获取凭据状态", description = "获取凭据状态")
    public ResponseResult<Integer> selectCredentialByUuid(@RequestHeader("Authorization") String token, @PathVariable String uuid) throws Exception {
        Integer status = credentialsService.selectCredentialByUuid(token.substring(7), uuid).getStatus();
        return ResponseResult.success(status);
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定凭据", description = "绑定凭据")
    public ResponseResult<Credential> updateCredentialConnectId(@RequestHeader("Authorization") String token, @RequestBody CredentialConnectionRequest request) throws Exception {
        return ResponseResult.success(credentialsService.updateCredentialConnectId(token.substring(7), request));
    }

    @GetMapping("/get/bound/{connectId}")
    @Operation(summary = "获取当前Connection的已绑定凭据", description = "获取当前Connection的已绑定凭据")
    public ResponseResult<List<Credential>> selectBoundCredentialsByConnectionId(@RequestHeader("Authorization") String token, @PathVariable Long connectId) throws Exception {
        return ResponseResult.success(credentialsService.selectBoundCredentialsByConnectionId(token.substring(7), connectId));
    }

    @GetMapping(value ="/installation/{uuid}", produces = "text/plain;charset=UTF-8")
    @Operation(summary = "安装脚本", description = "生成安装脚本")
    @AuthAccess
    public String generateInstallShell(@RequestParam String token, @RequestParam String endpoint,@PathVariable String uuid) throws Exception {
        return credentialsService.generateInstallShell(token, uuid, endpoint);
    }

}
