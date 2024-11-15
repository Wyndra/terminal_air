package top.srcandy.candyterminal.bean.dto;

import lombok.Getter;

@Getter
public class AddNewConnectDTO {
    private String host;
    private String port;
    private String username;
    private String password;
    private String name;
    private String method;
}
