package com.devloom.ai.toolbox.investment.common.infra.marketdata.config;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare.AkshareApi;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare.AkshareProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 金融数据服务配置。
 *
 * @author devloom
 */
@Configuration
@EnableConfigurationProperties({MarketDataProperties.class, AkshareProperties.class})
public class MarketDataConfig {

    /**
     * 创建 AkshareApi 代理 Bean。
     *
     * <p>使用 HttpServiceProxyFactory 将接口绑定到 RestClient。</p>
     */
    @Bean
    public AkshareApi akshareApi(AkshareProperties properties) {
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getGatewayUrl())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(AkshareApi.class);
    }
}
