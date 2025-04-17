package top.srcandy.terminal_air.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import top.srcandy.terminal_air.utils.JWTUtil;

import java.util.Map;


@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 握手之前 判断是否有token token中包含了用户信息
        if (request instanceof ServletServerHttpRequest){
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token == null){
                return false;
            }
            try {
                // 在WebSocket层验证token
                JWTUtil.validateToken(token);
                String username = JWTUtil.getTokenClaimMap(token).get("username").asString();
                attributes.put("username", username);
                log.info("{} 建立WebSocket连接", username);
            }catch (Exception e) {
                return false;
            }
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
