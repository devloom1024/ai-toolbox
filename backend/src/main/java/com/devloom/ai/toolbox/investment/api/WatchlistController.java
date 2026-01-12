package com.devloom.ai.toolbox.investment.api;

import com.devloom.ai.toolbox.common.response.ApiResponse;
import com.devloom.ai.toolbox.investment.domain.enums.Market;
import com.devloom.ai.toolbox.investment.dto.request.WatchlistAddRequest;
import com.devloom.ai.toolbox.investment.dto.request.WatchlistGroupCreateRequest;
import com.devloom.ai.toolbox.investment.dto.response.StockSearchResult;
import com.devloom.ai.toolbox.investment.dto.response.WatchlistGroup;
import com.devloom.ai.toolbox.investment.dto.response.WatchlistResponse;
import com.devloom.ai.toolbox.investment.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自选股管理 API
 *
 * @author huangkl
 */
@RestController
@RequestMapping("/api/v1/investment/watchlist")
@RequiredArgsConstructor
@Slf4j
public class WatchlistController {

    private final WatchlistService watchlistService;

    /**
     * 获取自选股列表
     *
     * @param groupId 分组 ID
     * @param market  市场类型
     * @return 自选股分页列表
     */
    @GetMapping
    public ApiResponse<WatchlistResponse> getWatchlist(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Market market) {
        log.debug("获取自选股列表，groupId={}, market={}", groupId, market);
        WatchlistResponse response = watchlistService.getWatchlist(groupId, market);
        return ApiResponse.success(response);
    }

    /**
     * 添加自选股
     *
     * @param request 添加请求
     * @return 成功响应
     */
    @PostMapping
    public ApiResponse<Void> addWatchlist(@Valid @RequestBody WatchlistAddRequest request) {
        log.info("添加自选股，request={}", request);
        watchlistService.addWatchlist(request);
        return ApiResponse.success();
    }

    /**
     * 移除自选股
     *
     * @param id 自选股 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeWatchlist(@PathVariable Long id) {
        log.info("移除自选股，id={}", id);
        watchlistService.removeWatchlist(id);
        return ApiResponse.success();
    }

    /**
     * 获取自选分组列表
     *
     * @return 分组列表
     */
    @GetMapping("/group")
    public ApiResponse<List<WatchlistGroup>> getGroupList() {
        log.debug("获取自选分组列表");
        List<WatchlistGroup> groups = watchlistService.getGroupList();
        return ApiResponse.success(groups);
    }

    /**
     * 创建自选分组
     *
     * @param request 创建请求
     * @return 创建的分组
     */
    @PostMapping("/group")
    public ApiResponse<WatchlistGroup> createGroup(@Valid @RequestBody WatchlistGroupCreateRequest request) {
        log.info("创建自选分组，request={}", request);
        WatchlistGroup group = watchlistService.createGroup(request.getName(), request.getSortOrder());
        return ApiResponse.success(group);
    }

    /**
     * 删除自选分组
     *
     * @param id 分组 ID
     * @return 成功响应
     */
    @DeleteMapping("/group/{id}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        log.info("删除自选分组，id={}", id);
        watchlistService.deleteGroup(id);
        return ApiResponse.success();
    }

    /**
     * 搜索股票
     *
     * @param keyword 搜索关键词
     * @param market  市场类型
     * @return 搜索结果列表
     */
    @GetMapping("/search")
    public ApiResponse<List<StockSearchResult>> searchStock(
            @RequestParam String keyword,
            @RequestParam(required = false) Market market) {
        log.debug("搜索股票，keyword={}, market={}", keyword, market);
        List<StockSearchResult> results = watchlistService.searchStock(keyword, market);
        return ApiResponse.success(results);
    }
}
