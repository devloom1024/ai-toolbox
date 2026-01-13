package com.devloom.ai.toolbox.investment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 市场数据配置类
 * <p>
 * 配置缓存、HTTP客户端等基础设施组件
 */
@Configuration
@EnableConfigurationProperties(MarketDataProperties.class)
@Import({
    MarketDataProperties.class,
    SearchProperties.class,
    CacheConfig.class,
    DataSourceConfig.class,
    CircuitBreakerConfig.class
})
public class MarketDataConfig {

    /**
     * 配置RestTemplate (用于HTTP调用Python服务)
     */
    @Bean
    public RestTemplate restTemplate(MarketDataProperties properties) {
        RestTemplate restTemplate = new RestTemplate();

        // 从配置获取超时时间,默认5秒
        int timeout = 5000;
        if (properties.getDataSources() != null && !properties.getDataSources().isEmpty()) {
            var aShareSources = properties.getDataSources().get("a-share");
            if (aShareSources != null && !aShareSources.isEmpty()) {
                timeout = aShareSources.get(0).getTimeout();
            }
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }

    /**
     * 配置RedisTemplate (用于L2缓存)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置序列化
        template.setKeySerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
        template.setValueSerializer(new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
        template.setHashValueSerializer(new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
