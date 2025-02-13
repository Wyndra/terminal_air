package top.srcandy.candyterminal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "top.srcandy.candyterminal")
@EnableScheduling
public class CandyTerminalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CandyTerminalApplication.class, args);
    }

}
