package com.devloom.ai.toolbox.investment.watchlist.domain.repository;

import com.devloom.ai.toolbox.investment.watchlist.domain.entity.WatchlistEntity;
import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 自选记录数据访问接口。
 *
 * @author claude
 */
@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistEntity, Long> {

    /**
     * 根据用户 ID、标的代码和市场类型查找自选记录。
     *
     * @param userId 用户 ID
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 自选记录（如果存在）
     */
    Optional<WatchlistEntity> findByUserIdAndSymbolAndMarket(Long userId, String symbol, MarketType market);

    /**
     * 检查用户是否已添加该标的到自选。
     *
     * @param userId 用户 ID
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 是否存在
     */
    boolean existsByUserIdAndSymbolAndMarket(Long userId, String symbol, MarketType market);

    /**
     * 根据用户 ID 分页查询自选记录。
     *
     * @param userId 用户 ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<WatchlistEntity> findAllByUserId(Long userId, Pageable pageable);

    /**
     * 根据用户 ID 和分组 ID 分页查询自选记录。
     *
     * @param userId 用户 ID
     * @param groupId 分组 ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<WatchlistEntity> findAllByUserIdAndGroupId(Long userId, Long groupId, Pageable pageable);

    /**
     * 将指定分组的标的移动到目标分组。
     *
     * @param sourceGroupId 源分组 ID
     * @param targetGroupId 目标分组 ID
     */
    @Modifying
    @Query("UPDATE WatchlistEntity w SET w.groupId = :targetGroupId WHERE w.groupId = :sourceGroupId")
    void moveToGroup(@Param("sourceGroupId") Long sourceGroupId, @Param("targetGroupId") Long targetGroupId);
}
