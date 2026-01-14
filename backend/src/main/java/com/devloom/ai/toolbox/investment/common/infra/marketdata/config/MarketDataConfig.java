package com.devloom.ai.toolbox.investment.common.infra.marketdata.config;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare.AkshareProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 金融数据服务配置。
 *
 * @author claude
 */
@Configuration
@EnableConfigurationProperties({MarketDataProperties.class, AkshareProperties.class})
public class MarketDataConfig {

    /**
     * 创建 Akshare Python 网关 RestClient Bean。
     */
    @Bean
    public RestClient akshareRestClient(AkshareProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getGatewayUrl())
                .build();
    }
}
