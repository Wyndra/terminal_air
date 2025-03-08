package top.srcandy.candyterminal.controller;

import com.rabbitmq.client.AMQP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.srcandy.candyterminal.aspectj.lang.annoations.AuthAccess;
import top.srcandy.candyterminal.service.NotificationConsumerService;
import top.srcandy.candyterminal.service.NotificationProducerService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationProducerService notificationProducerService;

    @Autowired
    private NotificationConsumerService notificationConsumerService;

    @GetMapping("/send")
    @AuthAccess
    public String sendNotification(String message) {
        notificationProducerService.sendNotification(message);
        return "send success";
    };

    @GetMapping("/receiveMessage")
    @AuthAccess
    public String receiveMessage(@RequestParam String userId) {
        // 从消息topicExchange队列中获取消息
        notificationConsumerService.consumeNotification(userId);
        return "receive success";
    }
}
