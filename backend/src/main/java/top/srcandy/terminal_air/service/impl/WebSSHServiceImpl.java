package top.srcandy.terminal_air.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import top.srcandy.terminal_air.pojo.vo.WebSSHMessageResultVO;
import top.srcandy.terminal_air.enums.ANSIColor;
import top.srcandy.terminal_air.enums.ANSIStyle;
import top.srcandy.terminal_air.pojo.SSHConnectInfo;
import top.srcandy.terminal_air.pojo.WebSSHData;
import org.springframework.web.socket.TextMessage;
import top.srcandy.terminal_air.pojo.WebSocketTypeData;
import top.srcandy.terminal_air.service.WebSSHService;
import top.srcandy.terminal_air.utils.ANSIFormatterUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class WebSSHServiceImpl implements WebSSHService {

    // 使用 WebSocketSession 作为 key，这样每个标签页有独立的连接信息
    // 主要解决了在同一用户登录的情况下，打开了多个标签页的问题
    private static Map<WebSocketSession, SSHConnectInfo> sshMap = new ConcurrentHashMap<>();

    // 线程池，处理异步任务（如 SSH 连接和命令执行）
    private ExecutorService executorService = Executors.newCachedThreadPool();


    /**
     * 初始化 WebSocket 会话的 SSH 连接信息。
     *
     * @param session 当前 WebSocket 会话
     */
    @Override
    public void initConnection(WebSocketSession session) {
        String username = (String) session.getAttributes().get("username");

        // 检查当前 WebSocket 会话是否已有连接
        if (sshMap.containsKey(session)) {
            log.debug("用户 {} 的连接信息已存在", username);
            return;
        }

        // 创建新的 JSch 对象，并初始化连接信息
        JSch jSch = new JSch();
        SSHConnectInfo sshConnectInfo = new SSHConnectInfo();
        sshConnectInfo.setJSch(jSch);
        sshConnectInfo.setWebSocketSession(session);

        // 将当前 WebSocket 会话与连接信息关联
        sshMap.put(session, sshConnectInfo);
        log.info("初始化用户 {} 的连接信息", username);
    }

    /**
     * 处理接收到的数据。
     *
     * @param session 当前 WebSocket 会话
     * @param buffer  接收到的 WebSocket 数据
     */
    @Override
    public void receiveHandle(WebSocketSession session, String buffer) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        WebSSHData webSSHData = null;
        WebSocketTypeData webSocketTypeData = null;
        try {
            webSSHData = objectMapper.readValue(buffer, WebSSHData.class);
        } catch (IOException e) {
            if (buffer.contains("pong")) {
                return;
            }
            log.error("数据转换成对象失败,异常信息：{}", e.getMessage());
            return;
        }

        String username = (String) session.getAttributes().get("username");
        SSHConnectInfo sshConnectInfo = sshMap.get(session);
        if (sshConnectInfo == null) {
            log.error("未找到用户 {} 的连接信息 正在初始化...", username);
            initConnection(session);
            return;
        }

        // 根据操作类型判断执行逻辑
        if ("connect".equals(webSSHData.getOperate())) {
            WebSSHData finalWebSSHData = webSSHData;
            WebSSHServiceImpl proxy = (WebSSHServiceImpl) AopContext.currentProxy();
            executorService.execute(() -> {
                try {
                    proxy.connectToSSH(sshConnectInfo, finalWebSSHData, session, "", "", "");
                } catch (IOException | JSchException e) {
                    log.error("连接失败，异常信息：{}", e.getMessage(), e);
                    // TODO 实现报告错误
                    close(session);
                }
            });
        } else if ("command".equals(webSSHData.getOperate())) {
            String command = webSSHData.getCommand();
            try {
                transToSSH(sshConnectInfo.getChannel(), command);
            } catch (IOException e) {
                log.error("发送命令失败，异常信息：{}", e.getMessage());
                close(session);
            }
        } else {
            log.error("不支持的操作");
            close(session);
        }
    }

    @Scheduled(fixedRate = 5000)
    @Override
    public void sendPingToClient() throws IOException {
        sshMap.forEach((session, sshConnectInfo) -> {
            try {
                String pingMessage = "ping";
                sendMessage(session, pingMessage.getBytes(), "ping", "ping");
            } catch (IOException e) {
                log.error("发送 ping 消息失败，异常信息：{}", e.getMessage());
                close(session);
            }
        });
    }

    /**
     * 向客户端 WebSocket 发送数据。
     *
     * @param session 当前 WebSocket 会话
     * @param buffer  要发送的字节数据
     * @param type    消息类型
     * @param message 消息内容
     * @throws IOException 发送消息时可能发生的异常
     */
    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer, String type, String message) throws IOException {
        WebSSHMessageResultVO result = WebSSHMessageResultVO.builder()
                .type(type)
                .msg(message)
                .timestamp(System.currentTimeMillis())
                .data(new TextMessage(new String(buffer)))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = objectMapper.writeValueAsString(result);
        // TODO 添加一个是否发送的boolean。
        session.sendMessage(new TextMessage(jsonMessage));
    }

    /**
     * 关闭当前 WebSocket 会话及其 SSH 连接。
     *
     * @param session 当前 WebSocket 会话
     */
    @Override
    public void close(WebSocketSession session) {
        Optional.ofNullable(sshMap.get(session))
                .ifPresent(sshConnectInfo -> {
                    Optional.ofNullable(sshConnectInfo.getChannel())
                            .ifPresent(Channel::disconnect);
                    sshMap.remove(session);
                });
    }

    /**
     * 建立 SSH 连接并执行命令。
     *
     * @param sshConnectInfo   当前 SSH 连接信息
     * @param webSSHData       WebSocket 请求数据
     * @param webSocketSession 当前 WebSocket 会话
     * @throws JSchException 建立 SSH 会话时抛出的异常
     * @throws IOException   在通信过程中抛出的异常
     */
    public void connectToSSH(SSHConnectInfo sshConnectInfo, WebSSHData webSSHData, WebSocketSession webSocketSession, String decryptedPassword, String privateKey, String publicKey) throws JSchException, IOException {
        if (sshConnectInfo == null || sshConnectInfo.getJSch() == null) {
            log.error("SSHConnectInfo 为空，无法建立连接");
            return;
        }

        Session session = null;
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PubkeyAcceptedKeyTypes", "ssh-rsa,rsa-sha2-256,rsa-sha2-512");
        JSch jSch = sshConnectInfo.getJSch();

        jSch.removeAllIdentity();
        if (webSSHData.getMethod().equals(1)) {
            config.put("PreferredAuthentications", "publickey");
            if (privateKey != null && !privateKey.isEmpty()) {
                byte[] privateKeyBytes = privateKey.getBytes(StandardCharsets.UTF_8);
                jSch.addIdentity("id_rsa", privateKeyBytes, null, null);
            } else {
                log.error("私钥为空，无法使用私钥登录");
                return;
            }
        }
        String username = (String) webSocketSession.getAttributes().get("username");  // 获取用户名作为 UUID
        log.info("{} 正在尝试连接 SSH 主机: {}:{}", username, webSSHData.getHost(), webSSHData.getPort());
        try {
            session = jSch.getSession(webSSHData.getUsername(), webSSHData.getHost(), webSSHData.getPort());
            session.setConfig(config);
            if (webSSHData.getMethod().equals(0)) {
                session.setPassword(decryptedPassword);
            }
            ;
            session.connect(30000);
            log.debug("{} SSH 会话连接成功", username);
            Channel channel = session.openChannel("shell");
            channel.connect(3000);
            log.debug("{} SSH 通道连接成功", username);

            sshConnectInfo.setChannel(channel);
            transToSSH(channel, webSSHData.getCommand());
            log.debug("{} SSH 通道已发送命令", username);

            InputStream inputStream = channel.getInputStream();
            try {
                byte[] buffer = new byte[1024];
                int i;
                while ((i = inputStream.read(buffer)) != -1) {
                    sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i), "stdout", "success");  // 将输出发送给 WebSocket 客户端
                }
            } finally {
                // 关闭连接和流
                if (session.isConnected()) {
                    session.disconnect();
                    log.debug("{} SSH 会话断开", username);
                }
                if (channel.isConnected()) {
                    channel.disconnect();
                    log.debug("{} SSH 通道断开", username);

                }
                if (inputStream != null) {
                    inputStream.close();
                    log.debug("{} 输入流已关闭", username);
                }
            }
        } catch (Exception e) {
            log.error("{} SSH 连接异常：{}", username, e.getMessage(), e);
            String errorMessage = ANSIFormatterUtil.formatMessage("✘ " + e.getMessage() + "\n\r", ANSIColor.BRIGHT_RED, ANSIStyle.BOLD);
            byte[] buffer = errorMessage.getBytes();
            sendMessage(webSocketSession, buffer, "error", e.getMessage());
        }
    }

    /**
     * 将命令发送到 SSH 通道。
     *
     * @param channel 当前 SSH 通道
     * @param command 要执行的命令
     * @throws IOException 发送命令时可能抛出的异常
     */
    private void transToSSH(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();  // 确保命令被发送
        }
    }
}
