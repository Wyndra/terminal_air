package top.srcandy.candyterminal.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Data
public class RegisterDTO {
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;
    @NotEmpty(message = "密码不能为空")
//    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)(?![^0-9a-zA-Z]+$).{8,20}$", message = "密码必须包含数字、大小写字母、特殊字符中的两种")
//    @Length(min = 8, max = 20, message = "密码长度必须在8-20之间")
    // md5
    @Length(min = 32, max = 32, message = "请将密码进行md5加密，再进行传输")
    @Pattern(regexp = "^[a-f0-9]{32}$", message = "密码必须是32位的md5加密字符串")
    private String password;
}
