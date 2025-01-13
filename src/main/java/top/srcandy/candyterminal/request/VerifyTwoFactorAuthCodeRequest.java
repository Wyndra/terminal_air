package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class VerifyTwoFactorAuthCodeRequest {
    private long code;
    private long time;
}