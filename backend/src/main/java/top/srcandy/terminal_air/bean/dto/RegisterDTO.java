package top.srcandy.terminal_air.bean.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String phone;
    private String salt;
    private String twoFactorSecret;
}
