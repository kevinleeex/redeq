package redeq.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/5
 */
@Configuration
@Slf4j
public class RedissonConfig {
    @Value("${redeq.redis.hosts}")
    private String hosts;
    @Value("${redeq.redis.password:}")
    private String password;

    @Bean(destroyMethod = "")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String[] hostArray = Arrays.stream(hosts.split(","))
                .map(x -> "redis://" + x.trim())
                .toArray(String[]::new);
        config.useClusterServers().addNodeAddress(hostArray);
        log.info("Redis cluster nodes added: {}", Arrays.toString(hostArray));
        return Redisson.create(config);
    }
}
