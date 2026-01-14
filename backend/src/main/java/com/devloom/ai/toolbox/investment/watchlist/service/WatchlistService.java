package com.devloom.ai.toolbox.investment.watchlist.service;

import com.devloom.ai.toolbox.common.dto.PageResponse;
import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import com.devloom.ai.toolbox.investment.watchlist.domain.entity.WatchlistEntity;
import com.devloom.ai.toolbox.investment.watchlist.domain.entity.WatchlistGroupEntity;
import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.watchlist.domain.repository.WatchlistGroupRepository;
import com.devloom.ai.toolbox.investment.watchlist.domain.repository.WatchlistRepository;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.AddWatchlistRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.CreateGroupRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.GetWatchlistRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.UpdateGroupRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.request.UpdateWatchlistRequest;
import com.devloom.ai.toolbox.investment.watchlist.dto.response.WatchlistCheckResponse;
import com.devloom.ai.toolbox.investment.watchlist.dto.response.WatchlistGroupResponse;
import com.devloom.ai.toolbox.investment.watchlist.dto.response.WatchlistItemResponse;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自选服务。
 *
 * @author claude
 */
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistGroupRepository watchlistGroupRepository;

    /**
     * 获取用户自选列表。
     */
    @Transactional(readOnly = true, rollbackFor = {Exception.class, Error.class})
    public PageResponse<WatchlistItemResponse> getWatchlist(Long userId, GetWatchlistRequest request) {
        WatchlistQuery query = WatchlistQuery.builder()
                .groupId(request.getGroupId())
                .market(request.getMarket())
                .page(request.getPage())
                .size(request.getSize())
                .build();

        int pageNum = query.getPageZeroBased();
        int pageSize = query.getPageSize();
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "addedAt"));

        Page<WatchlistEntity> entityPage;
        if (query.getGroupId() != null) {
            entityPage = watchlistRepository.findAllByUserIdAndGroupId(userId, query.getGroupId(), pageRequest);
        } else {
            entityPage = watchlistRepository.findAllByUserId(userId, pageRequest);
        }

        List<WatchlistItemResponse> items = entityPage.getContent().stream()
                .map(this::toWatchlistItem)
                .collect(Collectors.toList());

        int actualPage = query.getPageOneBased();
        return new PageResponse<>(actualPage, pageSize, entityPage.getTotalElements(), items);
    }

    /**
     * 添加自选。
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public WatchlistItemResponse addWatchlist(Long userId, AddWatchlistRequest request) {
        // 检查是否已存在
        if (watchlistRepository.existsByUserIdAndSymbolAndMarket(userId, request.getSymbol(), request.getMarket())) {
            throw new BizException(BizErrorCode.WATCHLIST_DUPLICATE_SYMBOL);
        }

        // 获取分组
        Long groupId = request.getGroupId();
        if (groupId == null) {
            WatchlistGroupEntity defaultGroup = watchlistGroupRepository.findByUserIdAndIsDefaultTrue(userId)
                    .orElseGet(() -> createDefaultGroup(userId));
            groupId = defaultGroup.getId();
        }

        WatchlistEntity entity = WatchlistEntity.builder()
                .userId(userId)
                .groupId(groupId)
                .symbol(request.getSymbol())
                .name("") // 名称从 Python 网关获取后更新
                .market(request.getMarket())
                .type(null) // 类型从 Python 网关获取后更新
                .note(request.getNote())
                .addedAt(Instant.now())
                .build();

        WatchlistEntity saved = watchlistRepository.save(entity);
        return toWatchlistItem(saved);
    }

    /**
     * 移除自选。
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void removeWatchlist(Long userId, Long id) {
        WatchlistEntity entity = watchlistRepository.findById(id)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        if (!entity.getUserId().equals(userId)) {
            throw new BizException(BizErrorCode.ACCESS_DENIED);
        }

        watchlistRepository.delete(entity);
    }

    /**
     * 更新自选信息。
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public WatchlistItemResponse updateWatchlist(Long userId, Long id, UpdateWatchlistRequest request) {
        WatchlistEntity entity = watchlistRepository.findById(id)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        if (!entity.getUserId().equals(userId)) {
            throw new BizException(BizErrorCode.ACCESS_DENIED);
        }

        if (request.getGroupId() != null) {
            entity.setGroupId(request.getGroupId());
        }
        if (request.getNote() != null) {
            entity.setNote(request.getNote());
        }

        WatchlistEntity saved = watchlistRepository.save(entity);
        return toWatchlistItem(saved);
    }

    /**
     * 检查是否已添加自选。
     */
    @Transactional(readOnly = true, rollbackFor = {Exception.class, Error.class})
    public WatchlistCheckResponse checkWatchlist(Long userId, String symbol, MarketType market) {
        return watchlistRepository.findByUserIdAndSymbolAndMarket(userId, symbol, market)
                .map(entity -> WatchlistCheckResponse.builder()
                        .inWatchlist(true)
                        .watchlistId(entity.getId())
                        .groupId(entity.getGroupId())
                        .build())
                .orElse(WatchlistCheckResponse.builder()
                        .inWatchlist(false)
                        .watchlistId(null)
                        .groupId(null)
                        .build());
    }

    /**
     * 获取分组列表。
     */
    @Transactional(readOnly = true, rollbackFor = {Exception.class, Error.class})
    public List<WatchlistGroupResponse> getGroups(Long userId) {
        return watchlistGroupRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(this::toWatchlistGroup)
                .collect(Collectors.toList());
    }

    /**
     * 创建分组。
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public WatchlistGroupResponse createGroup(Long userId, CreateGroupRequest request) {
        // 检查名称是否重复
        if (watchlistGroupRepository.findByUserIdAndName(userId, request.getName()).isPresent()) {
            throw new BizException(BizErrorCode.WATCHLIST_DUPLICATE_GROUP_NAME);
        }

        WatchlistGroupEntity entity = WatchlistGroupEntity.builder()
                .userId(userId)
                .name(request.getName())
                .isDefault(false)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        WatchlistGroupEntity saved = watchlistGroupRepository.save(entity);
        return toWatchlistGroup(saved);
    }

    /**
     * 更新分组。
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public WatchlistGroupResponse updateGroup(Long userId, Long id, UpdateGroupRequest request) {
        WatchlistGroupEntity entity = watchlistGroupRepository.findById(id)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        if (!entity.getUserId().equals(userId)) {
            throw new BizException(BizErrorCode.ACCESS_DENIED);
        }

        // 检查名称是否重复
        if (request.getName() != null && !request.getName().equals(entity.getName())) {
            if (watchlistGroupRepository.findByUserIdAndName(userId, request.getName()).isPresent()) {
                throw new BizException(BizErrorCode.WATCHLIST_DUPLICATE_GROUP_NAME);
            }
            entity.setName(request.getName());
        }

        WatchlistGroupEntity saved = watchlistGroupRepository.save(entity);
        return toWatchlistGroup(saved);
    }

    /**
     * 删除分组。
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteGroup(Long userId, Long id) {
        WatchlistGroupEntity entity = watchlistGroupRepository.findById(id)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        if (!entity.getUserId().equals(userId)) {
            throw new BizException(BizErrorCode.ACCESS_DENIED);
        }

        // 默认分组不能删除
        if (entity.getIsDefault()) {
            throw new BizException(BizErrorCode.INVALID_PARAMETER);
        }

        // 将组内标的移动到默认分组
        WatchlistGroupEntity defaultGroup = watchlistGroupRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        watchlistRepository.moveToGroup(id, defaultGroup.getId());
        watchlistGroupRepository.delete(entity);
    }

    private WatchlistGroupEntity createDefaultGroup(Long userId) {
        WatchlistGroupEntity entity = WatchlistGroupEntity.builder()
                .userId(userId)
                .name("默认分组")
                .isDefault(true)
                .sortOrder(0)
                .build();
        return watchlistGroupRepository.save(entity);
    }

    private WatchlistItemResponse toWatchlistItem(WatchlistEntity entity) {
        return WatchlistItemResponse.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .symbol(entity.getSymbol())
                .name(entity.getName())
                .market(entity.getMarket())
                .type(entity.getType())
                .note(entity.getNote())
                .currentPrice(entity.getCurrentPrice())
                .changePercent(entity.getChangePercent())
                .addedAt(entity.getAddedAt())
                .build();
    }

    private WatchlistGroupResponse toWatchlistGroup(WatchlistGroupEntity entity) {
        return WatchlistGroupResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isDefault(entity.getIsDefault())
                .securityCount(0) // TODO: 后续优化为统计查询
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
