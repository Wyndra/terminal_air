package top.srcandy.candyterminal.bean.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CredentialVO {
    @Schema(description = "凭据ID")
    private Long id;
    @Schema(description = "凭据UUID")
    private String uuid;
    @Schema(description = "凭据名称")
    private String name;
    @Schema(description = "标签")
    private String tags;
    @Schema(description = "凭据指纹")
    private String fingerprint;
    @Schema(description = "公钥")
    private String publicKey;
    @Schema(description = "连接ID")
    private Long connectId;
    @Schema(description = "创建时间")
    private String createTime;
    @Schema(description = "更新时间")
    private String updateTime;
}
