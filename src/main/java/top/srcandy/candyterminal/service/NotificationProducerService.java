package top.srcandy.candyterminal.service;

public interface NotificationProducerService {
    void sendNotification(String message);

    void sendNotification(String message, String routingKey);

    void sendNotification(String message, String routingKey, String exchange);
}
