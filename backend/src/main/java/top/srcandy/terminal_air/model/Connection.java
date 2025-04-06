package top.srcandy.terminal_air.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Connection implements Serializable {
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
    @Schema(description = "凭证ID")
    private Long credentialId;
    @Schema(description = "连接创建者id")
    private Long connect_creater_uid;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getConnectHost() {
        return connectHost;
    }

    public void setConnectHost(String connectHost) {
        this.connectHost = connectHost;
    }

    public String getConnectPort() {
        return connectPort;
    }

    public void setConnectPort(String connectPort) {
        this.connectPort = connectPort;
    }

    public String getConnectUsername() {
        return connectUsername;
    }

    public void setConnectUsername(String connectUsername) {
        this.connectUsername = connectUsername;
    }

    public String getConnectPwd() {
        return connectPwd;
    }

    public void setConnectPwd(String connectPwd) {
        this.connectPwd = connectPwd;
    }

    public String getConnectName() {
        return connectName;
    }

    public void setConnectName(String connectName) {
        this.connectName = connectName;
    }

    public String getConnectMethod() {
        return connectMethod;
    }

    public void setConnectMethod(String connectMethod) {
        this.connectMethod = connectMethod;
    }

    public Long getConnect_creater_uid() {
        return connect_creater_uid;
    }

    public void setConnect_creater_uid(Long connect_creater_uid) {
        this.connect_creater_uid = connect_creater_uid;
    }
}
