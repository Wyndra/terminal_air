package top.srcandy.candyterminal.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class VerifyTwoFactorAuthCodeRequest {

    @Schema(description = "二次验证码")
    @Length(min = 6, max = 6, message = "二次验证码长度必须为6位")
    private long code;

    @Schema(description = "当前时间戳")
    private long time;
}