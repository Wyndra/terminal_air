package top.srcandy.terminal_air.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.srcandy.terminal_air.service.CredentialsService;

@SpringBootTest
@Slf4j
class CredentialsServiceImplTest {

    @Autowired
    private CredentialsService credentialsService;


    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJUZXJtaW5hbCBBaXIiLCJleHAiOjE3NDMyMzU5MjgsImlhdCI6MTc0MzE0OTUyOCwidXNlcm5hbWUiOiJ3eW5kcmEifQ.1T1S4QFrjRd_EFPmqgORwuAeHrj3AVk2y4dIBvFqcQI";
    @Test
    void test_noPassphrase() throws Exception {
//        Credential credential = null;
//        try {
//            credential = credentialsService.generateKeyPair(token, "test", "test");
//        }catch (Exception e){
//            log.error("error: {}", e.getMessage());
//            assertEquals(1, 1);
//        }
//        log.info("credential:\n{}", credential.getPrivateKey());
//        log.info("credential:\n{}", credential.getPublicKey());
    }
}