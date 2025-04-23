package top.srcandy.terminal_air.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Null;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.terminal_air.pojo.vo.CredentialVo;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.pojo.model.Credential;
import top.srcandy.terminal_air.request.CredentialConnectionRequest;
import top.srcandy.terminal_air.request.CredentialStatusShortTokenRequest;
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
    public ResponseResult<CredentialVo> generateKeyPair(@RequestHeader("Authorization") String token, @RequestBody(required = false) @NonNull GenerateKeyPairRequest request) throws Exception {
        return ResponseResult.success(credentialsService.generateKeyPair(request.getName(), request.getTags()));
    }

    @GetMapping("/list")
    @Operation(summary = "凭据列表", description = "列出用户当前凭据")
    public ResponseResult<List<CredentialVo>> listCredentials() throws Exception {
        return ResponseResult.success(credentialsService.listCredentials());
    }

    @GetMapping("/delete/{uuid}")
    @Operation(summary = "删除凭据", description = "删除指定凭据")
    public ResponseResult<List<Credential>> deleteCredential(@PathVariable String uuid) throws Exception {
        credentialsService.deleteCredential(uuid);
        return ResponseResult.success();
    }

    @PostMapping("/update/status")
    @Operation(summary = "更新凭据状态", description = "更新凭据状态 curl请求更新凭据状态")
    public ResponseResult<Null> updateCredentialStatus(@RequestBody CredentialStatusRequest request) throws Exception {
        credentialsService.updateCredentialStatus(request);
        return ResponseResult.success(null);
    }

    @PostMapping("/update/status/shortToken")
    @Operation(summary = "shortToken更新凭据状态", description = "通过shortToken更新凭据状态")
    public ResponseResult<Null> updateCredentialStatusByShortToken(@RequestBody CredentialStatusShortTokenRequest request) throws Exception {
        credentialsService.updateCredentialStatusByShortToken(request);
        return ResponseResult.success(null);
    }

    @PostMapping("/get/status/{uuid}")
    @Operation(summary = "获取凭据状态", description = "获取凭据状态")
    public ResponseResult<Integer> selectCredentialByUuid(@PathVariable String uuid) throws Exception {
        Integer status = credentialsService.selectCredentialByUuid(uuid).getStatus();
        return ResponseResult.success(status);
    }

    @PostMapping("/get/status/shortToken/{uuid}")
    @Operation(summary = "shortToken获取凭据状态", description = "通过shortToken获取凭据状态")
    public ResponseResult<Integer> selectCredentialByUuidSkipAuth(@RequestHeader("Authorization") String shortToken,@PathVariable String uuid) throws Exception {
        String token = shortToken.substring(7);
        Integer status = credentialsService.getCredentialByUuidSkipAuth(uuid).getStatus();
        return ResponseResult.success(status);
    }

    @PostMapping("/bind")
    @Operation(summary = "绑定凭据", description = "绑定凭据")
    public ResponseResult<CredentialVo> updateCredentialConnectId(@RequestBody CredentialConnectionRequest request) throws Exception {
        return ResponseResult.success(credentialsService.updateCredentialConnectId(request));
    }

    @GetMapping("/get/bound/{uuid}")
    @Operation(summary = "获取当前Connection的已绑定凭据", description = "获取当前Connection的已绑定凭据")
    public ResponseResult<List<CredentialVo>> selectBoundCredentialsByConnectionId(@PathVariable String uuid) throws Exception {
        return ResponseResult.success(credentialsService.selectBoundCredentialsByConnectionId(uuid));
    }

    @GetMapping("/get/installation/url/{uuid}")
    @Operation(summary = "获取安装脚本", description = "获取安装脚本下载链接")
    public ResponseResult<String> getInstallShellUrl(@RequestParam String endpoint,@PathVariable String uuid) throws Exception {
        return ResponseResult.success(credentialsService.getInstallShellUrl(endpoint,uuid));
    }


    @GetMapping(value ="/installation/{uuid}", produces = "text/plain;charset=UTF-8")
    @Operation(summary = "安装脚本动态渲染", description = "动态安装脚本")
    public String generateInstallShell(@RequestParam String extra,@PathVariable String uuid) throws Exception {
        return credentialsService.generateInstallShell(uuid, extra);
    }

}
