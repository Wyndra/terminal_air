package top.srcandy.candyterminal.service;

import top.srcandy.candyterminal.constant.ResponseResult;

import java.util.Map;
import java.util.Objects;

public interface TurnstileService {
    public ResponseResult<Map<String, Objects>> verifyTurnstile(String token);
}
