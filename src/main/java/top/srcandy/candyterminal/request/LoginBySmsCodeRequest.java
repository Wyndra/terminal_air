package top.srcandy.candyterminal.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginBySmsCodeRequest {
    @Schema(description = "登录手机号")
    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "验证码序列号")
    @NotEmpty(message = "验证码序列号不能为空")
    @Length(min = 4, max = 4, message = "缺少验证码序列号")
    private String serial;

    @Schema(description = "验证码")
    @NotEmpty(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码长度必须为6位")
    private String verificationCode;
}
