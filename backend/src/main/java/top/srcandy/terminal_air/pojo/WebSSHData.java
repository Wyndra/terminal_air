package top.srcandy.terminal_air.pojo;

import lombok.Data;

@Data
public class WebSSHData {
    private String operate;
    private String host;
    private Integer port = 22;
    private Integer method = 0;
    private String username;
    private String password;
    private String credentialUUID;
    private String command = "";
    private boolean silent;
}
