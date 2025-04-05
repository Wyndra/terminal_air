package top.srcandy.terminal_air.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface WebSSHService {
    public void initConnection(WebSocketSession session);
    public void receiveHandle(WebSocketSession session, String buffer) throws JsonProcessingException;

    public void sendPingToClient() throws IOException;

    public void sendMessage(WebSocketSession session, byte[] buffer,String type,String message) throws IOException;

    public void close(WebSocketSession session);
}
