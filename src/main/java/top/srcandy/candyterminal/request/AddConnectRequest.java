package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class AddConnectRequest {
    private String host;
    private String port;
    private String username;
    private String password;
    private String name;
    private String method;
}
