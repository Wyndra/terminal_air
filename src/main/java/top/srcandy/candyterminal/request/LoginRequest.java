package top.srcandy.candyterminal.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequest {
    @NotEmpty(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;
    @NotEmpty(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;
}
