package top.srcandy.candyterminal.controller;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.request.GenerateKeyPairRequest;
import top.srcandy.candyterminal.service.CredentialsService;
import top.srcandy.candyterminal.utils.JWTUtil;

@RestController
@Slf4j
@Validated
@RequestMapping("/credentials")
public class CredentialsController {
    @Autowired
    private CredentialsService credentialsService;

    @PostMapping("/generateKeyPair")
    public ResponseResult<Credential> generateKeyPair(@RequestHeader("Authorization") String token, @RequestBody(required = false) @NonNull GenerateKeyPairRequest request) {
        try {
            return ResponseResult.success(credentialsService.generateKeyPair(token.substring(7), request.getConnectId(), request.getName(), request.getPassphrase()));
        } catch (Exception e) {
            log.error("generate key pair error", e);
//            return ResponseResult.fail(null);
            return ResponseResult.success();
        }
    }
}
