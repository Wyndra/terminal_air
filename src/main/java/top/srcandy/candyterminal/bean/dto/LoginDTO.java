package top.srcandy.candyterminal.bean.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;

@Data
public class LoginDTO {
    @NotEmpty(message = "用户名不能为空")
    private String username;
    @NotEmpty(message = "密码不能为空")
    private String password;

    private String salt;
}
