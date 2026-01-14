package com.devloom.ai.toolbox.investment.security.api;

import com.devloom.ai.toolbox.common.response.ApiResponse;
import com.devloom.ai.toolbox.investment.security.dto.response.QuoteDataResponse;
import com.devloom.ai.toolbox.investment.security.dto.response.SecurityBasicInfo;
import com.devloom.ai.toolbox.investment.security.service.SecurityService;
import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 标的详情接口控制器。
 *
 * @author claude
 */
@RestController
@RequestMapping("/api/v1/investment/security")
@RequiredArgsConstructor
@Validated
public class SecurityController {

    private final SecurityService securityService;

    /**
     * GET /api/v1/investment/security/{symbol}
     * <p>获取标的的详细信息，包括实时行情、基本面数据等。</p>
     */
    @GetMapping("/{symbol}")
    public ApiResponse<SecurityDetailResponse> getDetail(
            @PathVariable String symbol,
            @RequestParam MarketType market) {
        SecurityBasicInfo basicInfo = securityService.getBasicInfo(symbol, market);
        QuoteDataResponse quote = securityService.getQuote(symbol, market);

        SecurityDetailResponse response = SecurityDetailResponse.builder()
                .basicInfo(basicInfo)
                .quote(quote)
                .build();

        return ApiResponse.success(response);
    }

    /**
     * GET /api/v1/investment/security/{symbol}/quote
     * <p>获取标的的实时行情数据。</p>
     */
    @GetMapping("/{symbol}/quote")
    public ApiResponse<QuoteDataResponse> getQuote(
            @PathVariable String symbol,
            @RequestParam MarketType market) {
        QuoteDataResponse result = securityService.getQuote(symbol, market);
        return ApiResponse.success(result);
    }

    // 内部响应类
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class SecurityDetailResponse {
        private SecurityBasicInfo basicInfo;
        private QuoteDataResponse quote;
    }
}
