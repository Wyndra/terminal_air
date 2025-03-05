package top.srcandy.candyterminal.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue notificationQueue() {
        return new Queue("notificationQueue", true);
    }

    @Bean
    public DirectExchange notificationExchange() {
        // 是否持久化，是否自动删除?
        // 持久化什么？自动删除什么？
        // 持久化：重启后是否还存在
        // 自动删除：最后一个消费者断开后，是否自动删除
        return new DirectExchange("notificationExchange", true, false);
    }

    // 绑定队列到交换机
    @Bean
    public Binding binding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("notify");
    }
}
