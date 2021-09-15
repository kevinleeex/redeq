package com.lidengju.redeq.config;

import com.lidengju.redeq.api.Redeq;
import com.lidengju.redeq.api.RedeqClient;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/4
 */
@Configuration
public class RedeqAutoConfig {
    @Bean
    public RedeqClient redeqClient(RedissonClient redissonClient, ConfigProperties configProperties) {
        RedeqConfig config = configProperties.getConfig();
        return new Redeq(redissonClient, config);
    }
}
