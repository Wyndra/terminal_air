package top.srcandy.candyterminal.bean.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginResultVO {
    private String token;
    private boolean requireTwoFactorAuth;
}
