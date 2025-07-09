package top.srcandy.terminal_air.pojo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import top.srcandy.terminal_air.pojo.vo.CredentialVo;

import java.io.Serializable;

@Data
@Builder
public class Connection implements Serializable {
    private static final long serialVersionUID = 5L;
    @Schema(description = "连接id")
    private Long id;
    @Schema(description = "连接uuid")
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
    @Schema(description = "连接方式")
    private String method;
    @Schema(description = "凭证ID")
    private Long credentialId;
    @Schema(description = "连接创建者id")
    private Long user_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }
}
