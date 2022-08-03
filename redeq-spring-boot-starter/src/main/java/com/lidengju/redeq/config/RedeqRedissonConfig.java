package com.lidengju.redeq.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/5
 */
@Configuration
@ConditionalOnProperty(prefix = "redeq.redis", name = "hosts")
public class RedeqRedissonConfig {

    private static final Logger log = LoggerFactory.getLogger(RedeqRedissonConfig.class);

    @Value("${redeq.redis.hosts}")
    private String hosts;
    @Value("${redeq.redis.password:}")
    private String password;

    @Bean
    @ConditionalOnMissingBean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String[] hostArray = Arrays.stream(hosts.split(","))
                .map(x -> "redis://" + x.trim())
                .toArray(String[]::new);
        config.useClusterServers().addNodeAddress(hostArray).setPassword(password);
        log.info("Redis cluster nodes added: {}", Arrays.toString(hostArray));
        return Redisson.create(config);
    }
}
