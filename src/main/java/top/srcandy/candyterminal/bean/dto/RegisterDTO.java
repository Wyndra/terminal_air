package top.srcandy.candyterminal.bean.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String phone;
    private String salt;
}
