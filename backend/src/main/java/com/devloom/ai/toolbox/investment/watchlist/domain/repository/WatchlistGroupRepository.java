package com.devloom.ai.toolbox.investment.watchlist.domain.repository;

import com.devloom.ai.toolbox.investment.watchlist.domain.entity.WatchlistGroupEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 自选分组数据访问接口。
 *
 * @author devloom
 */
public interface WatchlistGroupRepository extends JpaRepository<WatchlistGroupEntity, Long> {

    /**
     * 根据用户 ID 查找所有分组。
     *
     * @param userId 用户 ID
     * @return 分组列表
     */
    List<WatchlistGroupEntity> findByUserIdOrderBySortOrderAsc(Long userId);

    /**
     * 根据用户 ID 和分组名称查找分组。
     *
     * @param userId 用户 ID
     * @param name 分组名称
     * @return 分组（如果存在）
     */
    Optional<WatchlistGroupEntity> findByUserIdAndName(Long userId, String name);

    /**
     * 查找用户的默认分组。
     *
     * @param userId 用户 ID
     * @return 默认分组
     */
    Optional<WatchlistGroupEntity> findByUserIdAndIsDefaultTrue(Long userId);
}
