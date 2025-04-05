package top.srcandy.terminal_air.service;

import top.srcandy.terminal_air.constant.ResponseResult;

import java.util.Map;
import java.util.Objects;

public interface TurnstileService {
    public ResponseResult<Map<String, Objects>> verifyTurnstile(String token);
}
