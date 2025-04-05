package top.srcandy.terminal_air.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import jakarta.annotation.Nullable;

@Data
public class UpdateProfileRequest {
    private Long uid;

    @Nullable
    private String username;

    private String password;

    // 邮箱：如果提供了邮箱，则校验格式
    @Nullable
//    @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    private String salt;

    // 昵称：不能为空，且长度在 2 到 20 个字符之间
    @Nullable
    @Length(min = 2, max = 20, message = "昵称长度必须在2-20之间")
    private String nickname;

    // 电话号码：如果提供了电话，则校验格式
    @Nullable
    @Pattern(regexp = "^1[3|4|5|7|8][0-9]\\d{8}$", message = "电话号码格式不正确")
    private String phone;

    @Nullable
    private String avatar;

    @Nullable
    private String isTwoFactorAuth;

    @Nullable
    private String twoFactorAuthSecret;
}
