package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class LoginBySmsCodeRequest {

    private String phone;
    private String serial;
    private String verificationCode;
}
