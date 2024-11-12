package top.srcandy.candyterminal.pojo;

import lombok.Data;

@Data
public class WebSSHData {
    private String operate;
    private String host;
    private Integer port = 22;
    private String username;
    private String password;
    private String command = "";
}
