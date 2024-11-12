package top.srcandy.candyterminal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "top.srcandy.candyterminal")
public class CandyTerminalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CandyTerminalApplication.class, args);
    }

}
