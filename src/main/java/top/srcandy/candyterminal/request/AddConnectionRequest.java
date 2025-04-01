package top.srcandy.candyterminal.request;

import lombok.Data;

@Data
public class AddConnectionRequest {
    private String host;
    private String port;
    private String username;
    private String password;
    private String name;
    private String method;
}
