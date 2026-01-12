package com.devloom.ai.toolbox.investment.service;

import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import com.devloom.ai.toolbox.common.security.CurrentUserHolder;
import com.devloom.ai.toolbox.investment.domain.entity.WatchlistEntity;
import com.devloom.ai.toolbox.investment.domain.entity.WatchlistGroupEntity;
import com.devloom.ai.toolbox.investment.domain.enums.Market;
import com.devloom.ai.toolbox.investment.domain.repository.WatchlistGroupRepository;
import com.devloom.ai.toolbox.investment.domain.repository.WatchlistRepository;
import com.devloom.ai.toolbox.investment.dto.request.WatchlistAddRequest;
import com.devloom.ai.toolbox.investment.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自选股管理服务
 *
 * @author huangkl
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistGroupRepository watchlistGroupRepository;

    /**
     * 获取自选股列表
     *
     * @param groupId 分组 ID，null 表示获取所有
     * @param market  市场类型
     * @return 自选股分页响应
     */
    public WatchlistResponse getWatchlist(Long groupId, Market market) {
        Long userId = CurrentUserHolder.getUserId();
        log.debug("获取用户 {} 的自选股列表，groupId={}, market={}", userId, groupId, market);

        List<WatchlistEntity> watchlistEntities;

        if (groupId != null) {
            // 指定分组
            watchlistEntities = watchlistRepository.findByUserIdAndGroupIdOrderBySortOrderAsc(userId, groupId);
        } else if (market != null) {
            // 指定市场类型
            watchlistEntities = watchlistRepository.findByUserIdAndMarketOrderBySortOrderAsc(userId, market);
        } else {
            // 获取所有
            watchlistEntities = watchlistRepository.findByUserIdOrderBySortOrderAsc(userId);
        }

        // 获取所有分组信息（用于填充 groupName）
        Map<Long, String> groupMap = watchlistGroupRepository.findByUserIdOrderBySortOrderAsc(userId)
                .stream()
                .collect(Collectors.toMap(WatchlistGroupEntity::getId, WatchlistGroupEntity::getName));

        // 转换为响应 DTO
        List<WatchlistItem> items = watchlistEntities.stream()
                .map(entity -> convertToWatchlistItem(entity, groupMap))
                .collect(Collectors.toList());

        return WatchlistResponse.builder()
                .page(1)
                .pageSize(items.size())
                .total((long) items.size())
                .items(items)
                .build();
    }

    /**
     * 添加自选股
     *
     * @param request 添加请求
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void addWatchlist(WatchlistAddRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        log.info("用户 {} 添加自选股，symbol={}, market={}",
                userId, request.getSymbol(), request.getMarket());

        // 检查是否已添加
        if (watchlistRepository.existsByUserIdAndSymbol(userId, request.getSymbol())) {
            throw new BizException(BizErrorCode.WATCHLIST_DUPLICATE_SYMBOL);
        }

        // 如果指定了分组，验证分组是否存在且属于当前用户
        if (request.getGroupId() != null) {
            watchlistGroupRepository.findByUserIdAndId(userId, request.getGroupId())
                    .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));
        }

        WatchlistEntity watchlist = WatchlistEntity.builder()
                .userId(userId)
                .groupId(request.getGroupId())
                .symbol(request.getSymbol())
                .market(request.getMarket())
                .name(request.getName() != null ? request.getName() : "")
                .sortOrder((short) 0)
                .build();

        watchlistRepository.save(watchlist);
        log.info("自选股添加成功，id={}", watchlist.getId());
    }

    /**
     * 移除自选股
     *
     * @param watchlistId 自选股 ID
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void removeWatchlist(Long watchlistId) {
        Long userId = CurrentUserHolder.getUserId();
        log.info("用户 {} 移除自选股，watchlistId={}", userId, watchlistId);

        WatchlistEntity watchlist = watchlistRepository.findByUserIdAndId(userId, watchlistId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        watchlistRepository.delete(watchlist);
        log.info("自选股移除成功，watchlistId={}", watchlistId);
    }

    /**
     * 获取自选分组列表
     *
     * @return 分组列表
     */
    public List<WatchlistGroup> getGroupList() {
        Long userId = CurrentUserHolder.getUserId();
        log.debug("获取用户 {} 的自选分组列表", userId);

        List<WatchlistGroupEntity> groups = watchlistGroupRepository.findByUserIdOrderBySortOrderAsc(userId);

        return groups.stream()
                .map(this::convertToGroupResponse)
                .collect(Collectors.toList());
    }

    /**
     * 创建自选分组
     *
     * @param request 创建请求
     * @return 创建的分组
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public WatchlistGroup createGroup(String name, Short sortOrder) {
        Long userId = CurrentUserHolder.getUserId();
        log.info("用户 {} 创建自选分组，name={}", userId, name);

        // 检查分组名称是否已存在
        if (watchlistGroupRepository.existsByUserIdAndName(userId, name)) {
            throw new BizException(BizErrorCode.WATCHLIST_DUPLICATE_GROUP_NAME);
        }

        WatchlistGroupEntity group = WatchlistGroupEntity.builder()
                .userId(userId)
                .name(name)
                .sortOrder(sortOrder != null ? sortOrder : (short) 0)
                .build();

        group = watchlistGroupRepository.save(group);
        log.info("自选分组创建成功，id={}", group.getId());

        return convertToGroupResponse(group);
    }

    /**
     * 删除自选分组
     *
     * @param groupId 分组 ID
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteGroup(Long groupId) {
        Long userId = CurrentUserHolder.getUserId();
        log.info("用户 {} 删除自选分组，groupId={}", userId, groupId);

        WatchlistGroupEntity group = watchlistGroupRepository.findByUserIdAndId(userId, groupId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        // 将该分组下的自选股设为未分组
        watchlistRepository.clearGroupIdByUserIdAndGroupId(userId, groupId);

        // 删除分组
        watchlistGroupRepository.delete(group);
        log.info("自选分组删除成功，groupId={}", groupId);
    }

    /**
     * 搜索股票（TODO: 接入外部数据源）
     *
     * @param keyword 搜索关键词
     * @param market  市场类型
     * @return 搜索结果列表
     */
    public List<StockSearchResult> searchStock(String keyword, Market market) {
        Long userId = CurrentUserHolder.getUserId();
        log.debug("用户 {} 搜索股票，keyword={}, market={}", userId, keyword, market);

        // TODO: 接入 akshare 或其他数据源进行真实搜索
        // 这里返回模拟数据用于测试
        return getMockSearchResults(keyword, market);
    }

    /**
     * 将实体转换为 WatchlistItem
     */
    private WatchlistItem convertToWatchlistItem(WatchlistEntity entity, Map<Long, String> groupMap) {
        // TODO: 查询当前价格和 AI 分析结果
        // 这里返回模拟数据用于测试
        return WatchlistItem.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .market(entity.getMarket())
                .name(entity.getName())
                .currentPrice(getMockPrice(entity.getSymbol()))
                .changePercent(getMockChangePercent())
                .recommendation(getMockRecommendation())
                .confidence(getMockConfidence())
                .groupId(entity.getGroupId())
                .groupName(entity.getGroupId() != null ? groupMap.get(entity.getGroupId()) : null)
                .addedAt(entity.getCreatedAt())
                .build();
    }

    /**
     * 将分组实体转换为响应 DTO
     */
    private WatchlistGroup convertToGroupResponse(WatchlistGroupEntity entity) {
        long itemCount = watchlistRepository.countByUserIdAndGroupId(entity.getUserId(), entity.getId());

        return WatchlistGroup.builder()
                .id(entity.getId())
                .name(entity.getName())
                .sortOrder(entity.getSortOrder())
                .itemCount((int) itemCount)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ===== Mock 数据方法（后续接入真实数据源后移除） =====

    private BigDecimal getMockPrice(String symbol) {
        // 模拟价格
        return new BigDecimal("1850.00");
    }

    private BigDecimal getMockChangePercent() {
        // 模拟涨跌幅
        return new BigDecimal("1.23");
    }

    private WatchlistItem.Recommendation getMockRecommendation() {
        // 模拟 AI 建议
        return WatchlistItem.Recommendation.BUILD_POSITION;
    }

    private Integer getMockConfidence() {
        // 模拟置信度
        return 78;
    }

    private List<StockSearchResult> getMockSearchResults(String keyword, Market market) {
        List<StockSearchResult> results = new ArrayList<>();

        // 模拟搜索结果
        if (keyword.contains("600519") || keyword.contains("贵州茅台") || keyword.contains("gzm")) {
            results.add(StockSearchResult.builder()
                    .symbol("600519")
                    .name("贵州茅台")
                    .market(Market.A_SHARE)
                    .type(StockSearchResult.SecurityType.STOCK)
                    .fullCode("SH600519")
                    .pinyin("GZMT")
                    .industry("白酒")
                    .build());
        }

        if (keyword.contains("000001") || keyword.contains("上证指数") || keyword.contains("sz")) {
            results.add(StockSearchResult.builder()
                    .symbol("000001")
                    .name("上证指数")
                    .market(Market.A_SHARE)
                    .type(StockSearchResult.SecurityType.INDEX)
                    .fullCode("SZ000001")
                    .pinyin("SZZS")
                    .industry("指数")
                    .build());
        }

        if (keyword.contains("300750") || keyword.contains("宁德时代") || keyword.contains("ndsd")) {
            results.add(StockSearchResult.builder()
                    .symbol("300750")
                    .name("宁德时代")
                    .market(Market.A_SHARE)
                    .type(StockSearchResult.SecurityType.STOCK)
                    .fullCode("SZ300750")
                    .pinyin("NDSD")
                    .industry("新能源")
                    .build());
        }

        if (keyword.contains("159915") || keyword.contains("创业板ETF") || keyword.contains("cyb")) {
            results.add(StockSearchResult.builder()
                    .symbol("159915")
                    .name("创业板ETF")
                    .market(Market.ETF)
                    .type(StockSearchResult.SecurityType.ETF)
                    .fullCode("SZ159915")
                    .pinyin("CYBETF")
                    .industry("创业板")
                    .build());
        }

        if (keyword.contains("AAPL") || keyword.contains("苹果") || keyword.contains("apple")) {
            results.add(StockSearchResult.builder()
                    .symbol("AAPL")
                    .name("苹果公司")
                    .market(Market.US)
                    .type(StockSearchResult.SecurityType.STOCK)
                    .fullCode("USAAPL")
                    .pinyin("APPL")
                    .industry("科技")
                    .build());
        }

        return results;
    }
}
