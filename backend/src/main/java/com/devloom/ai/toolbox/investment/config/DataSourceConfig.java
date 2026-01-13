package com.devloom.ai.toolbox.investment.config;

import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import lombok.Data;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 数据源配置
 */
@Data
public class DataSourceConfig {
    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源类型 (PYTHON_SERVICE, REST_API, GRPC)
     */
    private String type;

    /**
     * 服务URL
     */
    private String url;

    /**
     * API Key (可选)
     */
    private String apiKey;

    /**
     * 优先级 (数字越小优先级越高)
     */
    private Integer priority = 99;

    /**
     * 超时时间 (毫秒)
     */
    private Integer timeout = 5000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 限流配置 (每分钟请求数)
     */
    private Integer rateLimit = 60;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 额外配置
     */
    private Map<String, String> properties;
}
