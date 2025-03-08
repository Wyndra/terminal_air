package top.srcandy.candyterminal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import top.srcandy.candyterminal.service.NotificationConsumerService;


@Service
@Slf4j
public class NotificationConsumerServiceImpl implements NotificationConsumerService {
    @Override
    @RabbitListener(queues = "notificationQueue")
    public void consumeNotification(String userId) {
        log.info("用户{}收到了消息", userId);
    }
}
