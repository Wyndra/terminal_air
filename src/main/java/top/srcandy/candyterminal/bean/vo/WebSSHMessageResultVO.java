package top.srcandy.candyterminal.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
public class WebSSHMessageResultVO{
    private String type;
    private String msg;
    private String sessionId;
    private Long timestamp;
    private TextMessage data;
}
