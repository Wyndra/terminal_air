package top.srcandy.candyterminal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoDTO {
    private String username;
    private String email;
    private String nickname;
}
