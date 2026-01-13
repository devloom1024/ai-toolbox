package com.devloom.ai.toolbox.investment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 股票搜索配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "investment.market-data.search")
public class SearchProperties {

    /**
     * 搜索结果缓存时间 (秒)
     */
    private Integer cacheTtlSeconds = 300;

    /**
     * 热门股票缓存时间 (秒)
     */
    private Integer hotCacheTtlSeconds = 3600;

    /**
     * 默认返回数量
     */
    private Integer defaultLimit = 20;

    /**
     * 最大返回数量
     */
    private Integer maxLimit = 100;

    /**
     * 校验limit参数
     *
     * @param requested 请求的数量
     * @return 有效的数量
     */
    public int validateLimit(int requested) {
        if (requested <= 0) {
            return defaultLimit;
        }
        return Math.min(requested, maxLimit);
    }
}
