package com.devloom.ai.toolbox.investment.search.service;

import com.devloom.ai.toolbox.investment.search.dto.request.SearchRequest;
import com.devloom.ai.toolbox.investment.search.dto.response.SearchResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * 标的搜索服务。
 *
 * <p>实际搜索逻辑委托给 Python 网关，通过 RestTemplate 调用。</p>
 *
 * @author claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestTemplate restTemplate;

    // Python 网关地址，后续应从配置读取
    private static final String PYTHON_GATEWAY_URL = "http://localhost:8081";

    /**
     * 搜索标的。
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    @Transactional(readOnly = true, rollbackFor = {Exception.class, Error.class})
    public SearchResultResponse search(SearchRequest request) {
        log.info("Searching securities: keyword={}, market={}", request.getKeyword(), request.getMarket());

        try {
            // TODO: 调用 Python 网关获取搜索结果
            // 临时返回空结果，等 Python 网关实现后再对接
            SearchResultResponse result = SearchResultResponse.builder()
                    .securities(Collections.emptyList())
                    .total(0L)
                    .limit(request.getLimit())
                    .offset(request.getOffset())
                    .build();
            return result;
        } catch (Exception e) {
            log.error("Failed to search securities", e);
            throw new RuntimeException("Search failed", e);
        }
    }
}
