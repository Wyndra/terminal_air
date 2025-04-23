package top.srcandy.terminal_air.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginResultVo {
    private String token;
    private boolean requireTwoFactorAuth;
}
