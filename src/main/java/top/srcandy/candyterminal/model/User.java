package top.srcandy.candyterminal.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long uid;
    private String username;
    private String nickname;
    // deprecate password_hash field
    private String password_hash;
    private String password;
    private String salt;
    private String email;
    private String phone;
    private String avatar;
    private String isTwoFactorAuth;
    private String twoFactorAuthSecret;
    private Timestamp createTime;
}
