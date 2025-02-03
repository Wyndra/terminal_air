package top.srcandy.candyterminal.service;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface WebSSHService {
    public void initConnection(WebSocketSession session);
    public void receiveHandle(WebSocketSession session, String buffer);

    public void sendMessage(WebSocketSession session, byte[] buffer,String type,String message) throws IOException;

    public void close(WebSocketSession session);
}
