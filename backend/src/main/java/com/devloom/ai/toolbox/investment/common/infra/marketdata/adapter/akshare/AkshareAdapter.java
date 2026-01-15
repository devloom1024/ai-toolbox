package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.MarketDataAdapter;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.router.MarketDataFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Akshare 数据源适配器。
 *
 * <p>通过 Python Gateway 调用 akshare 库获取市场数据。</p>
 *
 * @author devloom
 */
@Slf4j
@Component
public class AkshareAdapter implements MarketDataAdapter {

    private final AkshareProperties properties;

    @Autowired
    public AkshareAdapter(AkshareProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getName() {
        return "akshare";
    }

    @Override
    public Set<MarketType> getSupportedMarkets() {
        // Akshare 主要支持 A 股市场
        return Set.of(MarketType.A_SHARE);
    }

    @Override
    public Set<MarketDataFeature> getSupportedFeatures() {
        // 支持所有功能
        return Set.of(
                MarketDataFeature.SEARCH,
                MarketDataFeature.KLINE,
                MarketDataFeature.FUNDAMENTAL,
                MarketDataFeature.FINANCIAL,
                MarketDataFeature.CAPITAL_FLOW
        );
    }

    @Override
    public boolean isAvailable() {
        // TODO: 实现健康检查，调用 Python Gateway 检查连通性
        return true;
    }
}
