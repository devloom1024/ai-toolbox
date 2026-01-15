package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.MarketDataAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Akshare 数据源适配器。
 *
 * @author devloom
 */
@Slf4j
@Component
public class AkshareAdapter implements MarketDataAdapter {

    @Autowired
    public AkshareAdapter(AkshareProperties properties) {
    }

    @Override
    public String getName() {
        return "akshare";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
