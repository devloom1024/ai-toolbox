package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Akshare 数据源配置。
 *
 * @author devloom
 */
@Data
@ConfigurationProperties(prefix = "investment.marketdata.akshare")
public class AkshareProperties {

    /** Python Gateway 基础 URL。 */
    private String gatewayUrl = "http://localhost:8081";

    /** 连接超时时间。 */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 读取超时时间。 */
    private Duration readTimeout = Duration.ofSeconds(30);

    /** 最大重试次数。 */
    private int maxRetries = 3;
}
