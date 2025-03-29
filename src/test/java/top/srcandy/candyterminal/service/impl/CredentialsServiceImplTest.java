package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.srcandy.candyterminal.model.Credential;
import top.srcandy.candyterminal.request.LoginRequest;
import top.srcandy.candyterminal.service.AuthService;
import top.srcandy.candyterminal.service.CredentialsService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class CredentialsServiceImplTest {

    @Autowired
    private CredentialsService credentialsService;


    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUZXJtaW5hbCBBaXIiLCJleHAiOjE3NDMyMzU5MjgsImlhdCI6MTc0MzE0OTUyOCwidXNlcm5hbWUiOiJ3eW5kcmEifQ.1T1S4QFrjRd_EFPmqgORwuAeHrj3AVk2y4dIBvFqcQI";
    @Test
    void test() throws Exception {
        Credential credential = credentialsService.generateKeyPair(token, 23L, "test", "");
        log.info("credential:\n{}", credential.getPrivateKey());
        log.info("credential:\n{}", credential.getPublicKey());
    }
}