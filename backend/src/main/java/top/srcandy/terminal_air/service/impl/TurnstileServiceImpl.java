package top.srcandy.terminal_air.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import top.srcandy.terminal_air.constant.ResponseResult;
import top.srcandy.terminal_air.service.TurnstileService;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class TurnstileServiceImpl implements TurnstileService {
    @Override
    public ResponseResult<Map<String, Objects>> verifyTurnstile(String token) {
        String secretKey = System.getenv("CLOUDFLARE_SECRET_KEY");
        if (secretKey == null || secretKey.isEmpty()) {
            return null;
        }
        log.info("CLOUDFLARE_SECRET_KEY:{}", secretKey);
        if (token == null || token.isEmpty()) {
            return ResponseResult.fail(null, "缺少 Token");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

        // 构建请求参数
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secretKey);
        params.add("response", token);

        // 发送请求
        Map<String, Objects> response = restTemplate.postForObject(url, params, Map.class);

        return ResponseResult.success(response);
    }
}
