package com.xxx.takeout.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 配置 Redis 单节点模式
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 如果需要配置其他模式，例如集群模式，请参考 Redisson 文档
        return Redisson.create(config);
    }
}
