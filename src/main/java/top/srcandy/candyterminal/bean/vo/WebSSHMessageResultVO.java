package top.srcandy.candyterminal.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.io.InputStream;

@Data
@AllArgsConstructor
public class WebSSHMessageResultVO extends InputStream {
    private String type;
    private String message;
    private TextMessage textMessage;

    @Override
    public int read() throws IOException {
        return 0;
    }
}
