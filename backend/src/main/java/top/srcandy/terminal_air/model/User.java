package top.srcandy.terminal_air.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "用户ID")
    private Long uid;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "密码_md5")
    private String password_hash;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "盐")
    private String salt;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "是否启用二次验证")
    private String isTwoFactorAuth;
    @Schema(description = "二次验证密钥")
    private String twoFactorAuthSecret;
    @Schema(description = "创建时间")
    private Timestamp createTime;
}
