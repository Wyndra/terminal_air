package top.srcandy.terminal_air.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ConnectionVo {
    @Schema(description = "连接ID")
    private Long id;
    @Schema(description = "连接UUID")
    private String uuid;
    @Schema(description = "连接主机")
    private String host;
    @Schema(description = "连接端口")
    private String port;
    @Schema(description = "连接用户名")
    private String username;
    @Schema(description = "连接密码")
    private String password;
    @Schema(description = "连接名称")
    private String name;
    @Schema(description = "认证方式，0表示密码认证，1表示密钥认证")
    private String method;
    @Schema(description = "凭证UUID")
    private String credentialUUID;
    @Schema(description = "凭证名称")
    private CredentialVo credential;
    @Schema(description = "连接创建者ID")
    private Long user_id;
}
