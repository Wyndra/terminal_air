package top.srcandy.candyterminal.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterRequest {
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;
    @NotEmpty(message = "密码不能为空")
    // md5
    @Length(min = 32, max = 32, message = "请将密码进行md5加密，再进行传输")
    @Pattern(regexp = "^[a-f0-9]{32}$", message = "密码必须是32位的md5加密字符串")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码长度必须为6位")
    private String verificationCode;


}
