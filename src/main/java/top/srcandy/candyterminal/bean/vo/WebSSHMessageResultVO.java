package top.srcandy.candyterminal.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.socket.TextMessage;

@Data
@AllArgsConstructor
public class WebSSHMessageResultVO {
    private String type;
    private String message;
    private TextMessage textMessage;
}
