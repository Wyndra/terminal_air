package top.srcandy.candyterminal.bean.vo;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class ConnectionVO {
    private Long cid;
    private String connectHost;
    private String connectPort;
    private String connectUsername;
    private String connectPwd;
    private String connectName;
    private String connectMethod;
    private String credentialUUID;
    private Long connect_creater_uid;
}
