package top.srcandy.candyterminal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Credential {
    @Schema(description = "凭据ID")
    private Long id;
    @Schema(description = "凭据名称")
    private String name;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "公钥")
    private String publicKey;
    @Schema(description = "私钥")
    private String privateKey;
    @Schema(description = "密码")
    private String passphrase;
    @Schema(description = "连接ID")
    private Long connectId;
    @Schema(description = "创建时间")
    private Timestamp createTime;
    @Schema(description = "更新时间")
    private Timestamp updateTime;
}
