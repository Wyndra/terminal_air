package top.srcandy.candyterminal.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ConnectInfo implements Serializable {
    private static final long serialVersionUID = 5L;
    private Long cid;
    private String connectHost;
    private String connectPort;
    private String connectUsername;
    private String connectPwd;
    private String connectName;
    private String connectMethod;
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
