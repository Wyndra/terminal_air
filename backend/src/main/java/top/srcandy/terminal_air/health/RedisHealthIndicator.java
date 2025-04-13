package top.srcandy.terminal_air.health;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.srcandy.terminal_air.service.RedisService;

@Component
@Endpoint(id = "redis")
public class RedisHealthIndicator extends AbstractHealthIndicator {

    @Autowired
    private RedisService redisService;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // Perform your Redis health check logic here
        boolean redisIsUp = checkRedisConnection();

        if (redisIsUp) {
            builder.up().withDetail("redis-status", "Redis is up and running");
        } else {
            builder.down().withDetail("redis-status", "Redis is down");
        }
    }

    private boolean checkRedisConnection() {
        try {
            // 使用RedisTemplate执行PING命令检查Redis服务是否可用
            String response = (String) redisService.execute((connection) -> connection.ping());
            return "PONG".equals(response); // 如果返回"PONG"，则认为Redis正常
        } catch (Exception e) {
            return false; // 发生异常时认为Redis不可用
        }
    }
}
