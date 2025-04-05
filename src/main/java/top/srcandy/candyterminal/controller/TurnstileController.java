package top.srcandy.candyterminal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.srcandy.candyterminal.aspectj.lang.annoations.AuthAccess;
import top.srcandy.candyterminal.constant.ResponseResult;
import top.srcandy.candyterminal.service.TurnstileService;

import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
@Validated
@RequestMapping("/api/turnstile")
@Tag(name = "Turnstile Service", description = "Cloudflare Turnstile 认证服务")
public class TurnstileController {

    @Autowired
    private TurnstileService turnstileService;
    @PostMapping("/verify")
    @Operation(summary = "提交验证")
    @AuthAccess
    public ResponseResult<Map<String, Objects>> verifyTurnstile(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null) {
            return ResponseResult.fail(null,"缺少token");
        }
        return turnstileService.verifyTurnstile(token);
    }
}
