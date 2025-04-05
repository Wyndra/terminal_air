package top.srcandy.candyterminal.request;

import io.swagger.v3.oas.annotations.media.Schema;


public class UpdateConnectionRequest {
    @Schema(description = "连接ID")
    private Long cid;
    @Schema(description = "主机")
    private String host;
    @Schema(description = "端口")
    private String port;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "连接名称")
    private String name;
    @Schema(description = "连接方式")
    private String method;
    @Schema(description = "凭证UUID")
    private String credentialUUID;


    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
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

    public String getCredentialUUID() {
        return credentialUUID;
    }

    public void setCredentialUUID(String credentialUUID) {
        this.credentialUUID = credentialUUID;
    }
}
