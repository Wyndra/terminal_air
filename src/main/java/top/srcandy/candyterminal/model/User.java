package top.srcandy.candyterminal.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long uid;
    private String username;
    private String nickname;
    private String password;
    private String salt;
    private String email;
    private Timestamp createTime;
}
