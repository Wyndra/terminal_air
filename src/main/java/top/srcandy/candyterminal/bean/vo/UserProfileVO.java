package top.srcandy.candyterminal.bean.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileVO {
    private Long uid;
    private String username;
    private String email;
    private String nickname;
    private String salt;
}
