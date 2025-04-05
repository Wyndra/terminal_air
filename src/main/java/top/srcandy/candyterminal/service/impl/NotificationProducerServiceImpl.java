package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.srcandy.candyterminal.service.NotificationProducerService;

@Service
@Slf4j
public class NotificationProducerServiceImpl implements NotificationProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendNotification(String message) {
        // 发送消息
        rabbitTemplate.convertAndSend("topicExchange", "notify.user.all", message);
        log.info("发送了消息: {}", message);
    }

    @Override
    public void sendNotification(String message, String routingKey) {
        // 发送消息
        rabbitTemplate.convertAndSend("notificationExchange", routingKey, message);
        log.info("发送了消息: {}", message);
    }

    @Override
    public void sendNotification(String message, String routingKey, String exchange) {
        // 发送消息
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.info("发送了消息: {}", message);
    }
}
