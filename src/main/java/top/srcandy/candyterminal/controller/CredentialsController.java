package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.bean.vo.CredentialsVO;
import top.srcandy.candyterminal.bean.vo.PageQueryResultVO;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.request.GenerateKeyPairRequest;
import top.srcandy.candyterminal.request.PageQueryRequest;
import top.srcandy.candyterminal.service.CredentialsService;
import top.srcandy.candyterminal.utils.JWTUtil;

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
    public ResponseResult<List<Credential>> deleteCredential(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            credentialsService.deleteCredential(token.substring(7), id);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("delete credential error", e);
            return ResponseResult.success();
        }
    }

}
