package top.srcandy.candyterminal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import top.srcandy.candyterminal.pojo.SSHConnectInfo;
import top.srcandy.candyterminal.pojo.WebSSHData;
import org.springframework.web.socket.TextMessage;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.WebSSHService;
import top.srcandy.candyterminal.utils.AESUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class WebSSHServiceImpl implements WebSSHService {

    // 使用 WebSocketSession 作为 key，这样每个标签页有独立的连接信息
    private static Map<WebSocketSession, SSHConnectInfo> sshMap = new ConcurrentHashMap<>();

    // 线程池
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private AuthService authService;

    @Override
    public void initConnection(WebSocketSession session) {
        String username = (String) session.getAttributes().get("username");

        // 检查当前 WebSocket 会话是否已有连接
        if (sshMap.containsKey(session)) {
            log.error("用户 {} 已经有一个活跃的连接", username);
            return;
        }

        // 创建新的 JSch 对象
        JSch jSch = new JSch();
        SSHConnectInfo sshConnectInfo = new SSHConnectInfo();
        sshConnectInfo.setJSch(jSch);
        sshConnectInfo.setWebSocketSession(session);

        // 将当前 WebSocket 会话与连接信息关联
        sshMap.put(session, sshConnectInfo);
        log.info("初始化用户 {} 的连接信息", username);
    }

    @Override
    public void receiveHandle(WebSocketSession session, String buffer) {
        ObjectMapper objectMapper = new ObjectMapper();
        WebSSHData webSSHData = null;
        try {
            // 读取WebSocket的数据，并转换成WebSSHData对象
            webSSHData = objectMapper.readValue(buffer, WebSSHData.class);
        } catch (IOException e) {
            log.error("数据转换成对象失败");
            log.error("异常信息：{}", e.getMessage());
            return;
        }

        String userId = (String) session.getAttributes().get("username");
        log.info("接收到用户 {} 的请求", userId);

        SSHConnectInfo sshConnectInfo = sshMap.get(session);
        if (sshConnectInfo == null) {
            log.error("未找到用户 {} 的连接信息", userId);
            // 重新初始化连接信息
            initConnection(session);
            return;
        }

        if ("connect".equals(webSSHData.getOperate())) {
            WebSSHData finalWebSSHData = webSSHData;
            executorService.execute(() -> {
                try {
                    connectToSSH(sshConnectInfo, finalWebSSHData, session);
                } catch (IOException | JSchException e) {
                    log.error("连接失败");
                    log.error("异常信息：{}", e.getMessage());
                    close(session);
                }
            });
        } else if ("command".equals(webSSHData.getOperate())) {
            String command = webSSHData.getCommand();
            try {
                transToSSH(sshConnectInfo.getChannel(), command);
            } catch (IOException e) {
                log.error("发送命令失败");
                log.error("异常信息：{}", e.getMessage());
                close(session);
            }
        } else {
            log.error("不支持的操作");
            close(session);
        }
    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    @Override
    public void close(WebSocketSession session) {
        SSHConnectInfo sshConnectInfo = sshMap.get(session);
        if (sshConnectInfo != null) {
            // 断开连接
            if (sshConnectInfo.getChannel() != null) {
                sshConnectInfo.getChannel().disconnect();
            }
            // 移除连接信息
            sshMap.remove(session);
        }
    }

    private void connectToSSH(SSHConnectInfo sshConnectInfo, WebSSHData webSSHData, WebSocketSession webSocketSession) throws JSchException, IOException {
        if (sshConnectInfo == null || sshConnectInfo.getJSch() == null) {
            log.error("SSHConnectInfo 为空，无法建立连接");
            return;
        }

        Session session = null;
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        JSch jSch = sshConnectInfo.getJSch();

        // 从 WebSocketSession 中获取 UUID, UUID就是用户名
        String uuid = (String) webSocketSession.getAttributes().get("username");
        log.info("尝试连接 SSH 主机: {}:{}", webSSHData.getHost(), webSSHData.getPort());

        try {
            // 获取Key
            String salt = authService.getSaltByUsername(uuid);
            log.info("获取到 {} 的盐值为：{}", uuid, salt);
            // 解密密码
            String decryptedPassword = AESUtils.decryptFromHex(webSSHData.getPassword(), salt);
            log.info("解密后的密码为：{}", decryptedPassword);

            // 创建SSH会话并设置配置
            session = jSch.getSession(webSSHData.getUsername(), webSSHData.getHost(), webSSHData.getPort());
            session.setConfig(config);
            session.setPassword(decryptedPassword);

            session.connect(30000);  // 设置连接超时 30秒
            log.info("SSH 会话连接成功");

            Channel channel = session.openChannel("shell");
            channel.connect(3000);  // 设置通道连接超时 3秒
            log.info("SSH 通道连接成功");

            sshConnectInfo.setChannel(channel);
            transToSSH(channel, webSSHData.getCommand());
            log.info("命令已发送到 SSH");

            InputStream inputStream = channel.getInputStream();
            try {
                byte[] buffer = new byte[1024];
                int i = 0;
                while ((i = inputStream.read(buffer)) != -1) {
                    sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
                }
            } finally {
                if (session.isConnected()) {
                    session.disconnect();
                    log.info("SSH 会话断开");
                }
                if (channel.isConnected()) {
                    channel.disconnect();
                    log.info("SSH 通道断开");
                }
                if (inputStream != null) {
                    inputStream.close();
                    log.info("输入流已关闭");
                }
            }
        } catch (Exception e) {
            log.error("SSH 连接异常：{}", e.getMessage());
            close(webSocketSession);
        }
    }

    private void transToSSH(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }
}
