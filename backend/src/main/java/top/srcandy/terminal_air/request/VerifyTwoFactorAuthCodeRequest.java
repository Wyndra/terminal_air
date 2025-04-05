package top.srcandy.terminal_air.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class VerifyTwoFactorAuthCodeRequest {

    @Schema(description = "二次验证码")
    @Max(value = 6, message = "二次验证码长度必须为6位")
    @Min(value = 6, message = "二次验证码长度必须为6位")
    private long code;

    @Schema(description = "当前时间戳")
    private long time;
}