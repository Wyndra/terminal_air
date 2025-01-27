package top.srcandy.candyterminal.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Credential {
    private Long id;
    private String name;
    private Long userId;
    private String publicKey;
    private String privateKey;
    private String passphrase;
    private Long connectId;
    private Timestamp createTime;
    private Timestamp updateTime;
}
