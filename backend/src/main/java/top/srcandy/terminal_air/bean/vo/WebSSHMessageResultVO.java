package top.srcandy.terminal_air.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.socket.TextMessage;

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
