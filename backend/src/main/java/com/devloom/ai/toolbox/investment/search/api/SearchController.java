package com.devloom.ai.toolbox.investment.search.api;

import com.devloom.ai.toolbox.common.response.ApiResponse;
import com.devloom.ai.toolbox.investment.search.dto.request.SearchRequest;
import com.devloom.ai.toolbox.investment.search.dto.response.SearchResultResponse;
import com.devloom.ai.toolbox.investment.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标的搜索接口控制器。
 *
 * @author devloom
 */
@RestController
@RequestMapping("/api/v1/investment/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    /**
     * GET /api/v1/investment/search
     * <p>根据关键词搜索 A股、港股、美股、ETF、基金，支持按名称或代码搜索。</p>
     */
    @GetMapping
    public ApiResponse<SearchResultResponse> search(@Valid @ModelAttribute SearchRequest request) {
        SearchResultResponse result = searchService.search(request);
        return ApiResponse.success(result);
    }
}
