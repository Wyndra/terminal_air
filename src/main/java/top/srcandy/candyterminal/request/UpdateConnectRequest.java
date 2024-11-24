package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class UpdateConnectRequest {
    private Long cid;
    private String host;
    private String port;
    private String username;
    private String password;
    private String name;
    private String method;
}
