package top.srcandy.terminal_air.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data

public class VerifyOneTimeBackupCodeRequest {
    @NotEmpty
    @Pattern(regexp = "^[A-Z0-9]{4}-[A-Z0-9]{4}$", message = "备份代码格式错误")
    private String code;
}
