package top.srcandy.terminal_air.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdatePasswordRequest {
    @Schema(description = "旧密码")
    private String oldPassword;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 8, max = 20, message = "密码长度必须在8-20之间")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,20}$", message = "密码必须包含数字、字母、特殊字符")
    @Schema(description = "新密码")
    private String newPassword;
}
