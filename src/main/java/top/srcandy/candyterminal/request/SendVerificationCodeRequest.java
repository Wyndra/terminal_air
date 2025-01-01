package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class SendVerificationCodeRequest {
    private String phone;
    private String channel;
}
