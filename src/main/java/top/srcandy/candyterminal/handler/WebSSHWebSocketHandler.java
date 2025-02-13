package top.srcandy.candyterminal.handler;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import top.srcandy.candyterminal.service.WebSSHService;

@Component
@Slf4j
public class WebSSHWebSocketHandler implements WebSocketHandler {

    @Autowired
    private WebSSHService webSSHService;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("用户{}连接成功", session.getAttributes().get("username"));
        // 调用初始化连接
        webSSHService.initConnection(session);

    }

    /**
     * @Description: 收到消息的回调处理函数
     * @Param: [session, message]
     * @return: void
     * @Author: Mints_candy
     * @Date: 2024/8/5
    */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage){
            log.info("接收到用户{}的消息{}", session.getAttributes().get("username"), message.getPayload());
            webSSHService.receiveHandle(session, message.getPayload().toString());
            // 调用处理消息
        }else if (message instanceof BinaryMessage){
            log.info("接收到用户{}的二进制消息{}", session.getAttributes().get("username"), message.getPayload());
            // 调用处理二进制消息
        }else if (message instanceof PongMessage){
            log.info("接收到用户{}的pong消息{}", session.getAttributes().get("username"), message.getPayload());
            // 调用处理pong消息
        }else {
            log.info("接收到用户{}的其他消息{}", session.getAttributes().get("username"), message.getPayload());
            // 调用处理其他消息
        }

    }

    /**
     * @Description: 出现错误的回调函数
     * @param session
     * @param exception
     * @Author: Mints_candy
     * @Date: 2024/8/5
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("用户{}连接发生错误", session.getAttributes().get("username"));
    }

    /**
     * @Description: 关闭连接的回调函数
     * @param session
     * @param closeStatus
     * @Author: Mints_candy
     * @Date: 2024/8/5
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("用户{}连接关闭", session.getAttributes().get("username"));
        // 调用service关闭连接
        webSSHService.close(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
