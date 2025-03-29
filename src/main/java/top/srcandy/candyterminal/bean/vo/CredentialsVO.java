package top.srcandy.candyterminal.bean.vo;

import lombok.Data;

@Data
public class CredentialsVO {
    private Long id;
    private String uuid;
    private String name;
    private String tags;
    private String fingerprint;
    private String publicKey;
//    private String privateKey;
    private Long connectId;
    private String createTime;
    private String updateTime;
}
