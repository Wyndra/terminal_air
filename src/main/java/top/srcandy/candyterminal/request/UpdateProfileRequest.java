package top.srcandy.candyterminal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateProfileRequest {
    private Long uid;
    private String username;
    private String password;
    @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;
    private String salt;
    // nickname 不能为空，长度在 2 到 20 个字符
    @NotBlank(message = "昵称不能为空")
    @Length(min = 2, max = 20, message = "昵称长度必须在2-20之间")
    private String nickname;
    // 电话号码
    @NotBlank(message = "电话号码不能为空")
    @Pattern(regexp = "^1[3|4|5|7|8][0-9]\\d{8}$", message = "电话号码格式不正确")
    private String phone;
    private String avatar;
}
