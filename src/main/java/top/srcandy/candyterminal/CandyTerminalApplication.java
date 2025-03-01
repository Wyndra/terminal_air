package top.srcandy.candyterminal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "top.srcandy.candyterminal")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class CandyTerminalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CandyTerminalApplication.class, args);
    }

}
