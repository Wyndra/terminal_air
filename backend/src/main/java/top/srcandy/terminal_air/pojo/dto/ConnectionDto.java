package top.srcandy.terminal_air.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.srcandy.terminal_air.pojo.model.Credential;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ConnectionDto implements Serializable {
    private static final long serialVersionUID = 5L;
    @Schema(description = "连接id")
    private Long cid;
    @Schema(description = "连接uuid")
    private String connectionUuid;
    @Schema(description = "连接主机")
    private String connectHost;
    @Schema(description = "连接端口")
    private String connectPort;
    @Schema(description = "连接用户名")
    private String connectUsername;
    @Schema(description = "连接密码")
    private String connectPwd;
    @Schema(description = "连接名称")
    private String connectName;
    @Schema(description = "连接方式")
    private String connectMethod;
    @Schema(description = "凭证")
    private Credential credential;
    @Schema(description = "连接创建者id")
    private Long connect_creater_uid;
}
