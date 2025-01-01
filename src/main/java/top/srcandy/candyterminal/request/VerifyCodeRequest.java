package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class VerifyCodeRequest {
    private String phone;
    private String serial;
    private String code;
}
