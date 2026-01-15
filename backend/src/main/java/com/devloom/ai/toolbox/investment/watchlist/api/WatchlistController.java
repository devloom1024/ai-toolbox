package com.devloom.ai.toolbox.investment.watchlist.api;

import com.devloom.ai.toolbox.common.dto.PageResponse;
import com.devloom.ai.toolbox.common.response.ApiResponse;
import com.devloom.ai.toolbox.common.security.CurrentUser;
import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.AddWatchlistRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.CreateGroupRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.GetWatchlistRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.UpdateGroupRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.UpdateWatchlistRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.response.WatchlistCheckResponse;
import com.devloom.ai.toolbox.investment.watchlist.dto.response.WatchlistGroupResponse;
import com.devloom.ai.toolbox.investment.watchlist.dto.response.WatchlistItemResponse;
import com.devloom.ai.toolbox.investment.watchlist.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自选管理接口控制器。
 *
 * @author devloom
 */
@RestController
@RequestMapping("/api/v1/investment/watchlist")
@RequiredArgsConstructor
@Validated
public class WatchlistController {

    private final WatchlistService watchlistService;

    /**
     * GET /api/v1/investment/watchlist
     * <p>获取当前用户的自选列表，支持按分组和市场过滤。</p>
     */
    @GetMapping
    public ApiResponse<PageResponse<WatchlistItemResponse>> getWatchlist(
            @AuthenticationPrincipal CurrentUser currentUser,
            @ModelAttribute GetWatchlistRequest query) {
        PageResponse<WatchlistItemResponse> result = watchlistService.getWatchlist(currentUser.getUserId(), query);
        return ApiResponse.success(result);
    }

    /**
     * POST /api/v1/investment/watchlist
     * <p>将标的添加到自选列表，默认添加到默认分组。</p>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WatchlistItemResponse> addWatchlist(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody AddWatchlistRequest request) {
        WatchlistItemResponse result = watchlistService.addWatchlist(currentUser.getUserId(), request);
        return ApiResponse.success(result);
    }

    /**
     * DELETE /api/v1/investment/watchlist/{id}
     * <p>从自选列表中移除指定标的。</p>
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeWatchlist(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long id) {
        watchlistService.removeWatchlist(currentUser.getUserId(), id);
        return ApiResponse.success(null);
    }

    /**
     * PUT /api/v1/investment/watchlist/{id}
     * <p>更新自选标的的备注或移动到其他分组。</p>
     */
    @PutMapping("/{id}")
    public ApiResponse<WatchlistItemResponse> updateWatchlist(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateWatchlistRequest request) {
        WatchlistItemResponse result = watchlistService.updateWatchlist(currentUser.getUserId(), id, request);
        return ApiResponse.success(result);
    }

    /**
     * GET /api/v1/investment/watchlist/check
     * <p>检查指定标的是否已在自选列表中。</p>
     */
    @GetMapping("/check")
    public ApiResponse<WatchlistCheckResponse> checkWatchlist(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam String symbol,
            @RequestParam MarketType market) {
        WatchlistCheckResponse result = watchlistService.checkWatchlist(currentUser.getUserId(), symbol, market);
        return ApiResponse.success(result);
    }

    /**
     * GET /api/v1/investment/watchlist/groups
     * <p>获取当前用户的所有自选分组及每个分组的标的数量。</p>
     */
    @GetMapping("/groups")
    public ApiResponse<List<WatchlistGroupResponse>> getGroups(@AuthenticationPrincipal CurrentUser currentUser) {
        List<WatchlistGroupResponse> result = watchlistService.getGroups(currentUser.getUserId());
        return ApiResponse.success(result);
    }

    /**
     * POST /api/v1/investment/watchlist/groups
     * <p>创建一个新的自选分组。</p>
     */
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WatchlistGroupResponse> createGroup(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateGroupRequest request) {
        WatchlistGroupResponse result = watchlistService.createGroup(currentUser.getUserId(), request);
        return ApiResponse.success(result);
    }

    /**
     * PUT /api/v1/investment/watchlist/groups/{id}
     * <p>重命名自选分组。</p>
     */
    @PutMapping("/groups/{id}")
    public ApiResponse<WatchlistGroupResponse> updateGroup(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateGroupRequest request) {
        WatchlistGroupResponse result = watchlistService.updateGroup(currentUser.getUserId(), id, request);
        return ApiResponse.success(result);
    }

    /**
     * DELETE /api/v1/investment/watchlist/groups/{id}
     * <p>删除自选分组，组内标的将移至默认分组。</p>
     */
    @DeleteMapping("/groups/{id}")
    public ApiResponse<Void> deleteGroup(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable Long id) {
        watchlistService.deleteGroup(currentUser.getUserId(), id);
        return ApiResponse.success(null);
    }
}
