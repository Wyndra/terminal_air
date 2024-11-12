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

}
