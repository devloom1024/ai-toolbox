package com.devloom.ai.toolbox.investment.domain.repository;

import com.devloom.ai.toolbox.investment.domain.entity.WatchlistEntity;
import com.devloom.ai.toolbox.investment.domain.enums.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 自选股数据访问接口
 *
 * @author huangkl
 */
@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistEntity, Long> {

    /**
     * 根据用户 ID 和分组 ID 查询自选股列表
     *
     * @param userId  用户 ID
     * @param groupId 分组 ID，null 表示未分组
     * @return 自选股列表
     */
    List<WatchlistEntity> findByUserIdAndGroupIdOrderBySortOrderAsc(Long userId, Long groupId);

    /**
     * 根据用户 ID 查询未分组的自选股列表
     *
     * @param userId 用户 ID
     * @return 未分组的自选股列表
     */
    List<WatchlistEntity> findByUserIdAndGroupIdIsNullOrderBySortOrderAsc(Long userId);

    /**
     * 根据用户 ID 和市场类型查询自选股列表
     *
     * @param userId 用户 ID
     * @param market 市场类型
     * @return 自选股列表
     */
    List<WatchlistEntity> findByUserIdAndMarketOrderBySortOrderAsc(Long userId, Market market);

    /**
     * 根据用户 ID 查询所有自选股列表（按排序号升序）
     *
     * @param userId 用户 ID
     * @return 自选股列表
     */
    List<WatchlistEntity> findByUserIdOrderBySortOrderAsc(Long userId);

    /**
     * 根据用户 ID 和自选股 ID 查询
     *
     * @param userId 用户 ID
     * @param id     自选股 ID
     * @return 自选股
     */
    Optional<WatchlistEntity> findByUserIdAndId(Long userId, Long id);

    /**
     * 根据用户 ID 和股票代码查询
     *
     * @param userId 用户 ID
     * @param symbol 股票代码
     * @return 自选股
     */
    Optional<WatchlistEntity> findByUserIdAndSymbol(Long userId, String symbol);

    /**
     * 检查用户是否已添加该股票到自选
     *
     * @param userId 用户 ID
     * @param symbol 股票代码
     * @return 是否存在
     */
    boolean existsByUserIdAndSymbol(Long userId, String symbol);

    /**
     * 统计用户自选股数量
     *
     * @param userId 用户 ID
     * @return 数量
     */
    long countByUserId(Long userId);

    /**
     * 统计用户某分组下的自选股数量
     *
     * @param userId  用户 ID
     * @param groupId 分组 ID
     * @return 数量
     */
    long countByUserIdAndGroupId(Long userId, Long groupId);

    /**
     * 根据用户 ID 和分组 ID 删除（用于删除分组时）
     *
     * @param userId  用户 ID
     * @param groupId 分组 ID
     */
    void deleteByUserIdAndGroupId(Long userId, Long groupId);

    /**
     * 批量更新分组的自选股（删除分组时将相关自选股设为未分组）
     *
     * @param userId  用户 ID
     * @param groupId 原分组 ID
     */
    @Query("UPDATE WatchlistEntity w SET w.groupId = null WHERE w.userId = :userId AND w.groupId = :groupId")
    void clearGroupIdByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
