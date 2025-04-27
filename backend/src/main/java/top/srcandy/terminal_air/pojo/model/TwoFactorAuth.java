package top.srcandy.terminal_air.pojo.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class TwoFactorAuth {
    private Long tfid;
    private Long uid;
    private String isTwoFactorAuth;
    private String twoFactorAuthSecret;
    private String twoFactorTitle;
    private String oneTimeCodeBackup;
    private Timestamp createTime;
}
