package com.devloom.ai.toolbox.investment.security.service;

import com.devloom.ai.toolbox.investment.security.dto.response.QuoteDataResponse;
import com.devloom.ai.toolbox.investment.security.dto.response.SecurityBasicInfo;
import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * 标的详情服务。
 *
 * <p>实际数据获取委托给 Python 网关。</p>
 *
 * @author claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final RestTemplate restTemplate;

    // Python 网关地址，后续应从配置读取
    private static final String PYTHON_GATEWAY_URL = "http://localhost:8081";

    /**
     * 获取标的基本信息。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 标的基本信息
     */
    @Transactional(readOnly = true, rollbackFor = {Exception.class, Error.class})
    public SecurityBasicInfo getBasicInfo(String symbol, MarketType market) {
        log.info("Getting basic info: symbol={}, market={}", symbol, market);
        // TODO: 调用 Python 网关获取标的基本信息
        return SecurityBasicInfo.builder()
                .symbol(symbol)
                .name("TODO: 待 Python 网关实现")
                .market(market)
                .build();
    }

    /**
     * 获取实时行情。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 实时行情
     */
    @Transactional(readOnly = true, rollbackFor = {Exception.class, Error.class})
    public QuoteDataResponse getQuote(String symbol, MarketType market) {
        log.info("Getting quote: symbol={}, market={}", symbol, market);
        // TODO: 调用 Python 网关获取实时行情
        return QuoteDataResponse.builder()
                .symbol(symbol)
                .build();
    }
}
