package top.srcandy.candyterminal.aspectj;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import top.srcandy.candyterminal.pojo.WebSSHData;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.utils.AESUtils;

@Aspect
@Service
@Slf4j
public class WebSSHServiceAspect {

    @Pointcut("execution(public * top.srcandy.candyterminal.service.impl.WebSSHServiceImpl.connectToSSH(..))")
    public void connectToSSHPointCut() {}

    @Autowired
    private AuthService authService;

    @Around("connectToSSHPointCut()")
    public Object aroundConnectToSSH(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // 确保参数数量和类型正确
        if (args.length >= 4 && args[1] instanceof WebSSHData webSSHData && args[2] instanceof WebSocketSession webSocketSession) {

            String username = (String) webSocketSession.getAttributes().get("username");
            String salt = authService.getSaltByUsername(username);
            String decryptedPassword = AESUtils.decryptFromHex(webSSHData.getPassword(), salt);
            args[3] = decryptedPassword;
        }

        // 执行目标方法，返回结果
        return joinPoint.proceed(args);
    }
}
