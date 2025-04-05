package top.srcandy.terminal_air;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "top.srcandy.terminal_air")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class CandyTerminalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CandyTerminalApplication.class, args);
    }

}
