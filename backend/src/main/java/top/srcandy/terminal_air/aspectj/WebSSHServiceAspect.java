package top.srcandy.terminal_air.aspectj;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import top.srcandy.terminal_air.pojo.WebSSHData;
import top.srcandy.terminal_air.service.AuthService;
import top.srcandy.terminal_air.service.CredentialsService;
import top.srcandy.terminal_air.utils.AESUtils;

@Aspect
@Service
@Slf4j
public class WebSSHServiceAspect {

    @Pointcut("execution(public * top.srcandy.terminal_air.service.impl.WebSSHServiceImpl.connectToSSH(..))")
    public void connectToSSHPointCut() {}

    @Autowired
    private AuthService authService;

    @Autowired
    private CredentialsService credentialsService;

    @Around("connectToSSHPointCut()")
    public Object aroundConnectToSSH(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // 确保参数数量和类型正确
        if (args.length >= 4 && args[1] instanceof WebSSHData webSSHData && args[2] instanceof WebSocketSession webSocketSession) {
            if (webSSHData.getMethod().equals(0)){
                String username = (String) webSocketSession.getAttributes().get("username");
                String salt = authService.getSaltByUsername(username);
                String decryptedPassword = AESUtils.decryptFromHex(webSSHData.getPassword(), salt);
                log.info("Decrypted password: {}", decryptedPassword);
                args[3] = decryptedPassword;
            }else if (webSSHData.getMethod().equals(1)) {
                args[4] = credentialsService.selectCredentialByUuid(webSSHData.getCredentialUUID()).getPrivateKey();
                args[5] = credentialsService.selectCredentialByUuid(webSSHData.getCredentialUUID()).getPublicKey();
            }
        }
        // 执行目标方法，返回结果
        return joinPoint.proceed(args);
    }
}
