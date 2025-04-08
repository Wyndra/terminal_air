package top.srcandy.terminal_air.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginResultVO {
    private String token;
    private boolean requireTwoFactorAuth;
}
