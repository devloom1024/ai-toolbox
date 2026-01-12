package com.devloom.ai.toolbox.investment.domain.repository;

import com.devloom.ai.toolbox.investment.domain.entity.WatchlistGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 自选分组数据访问接口
 *
 * @author huangkl
 */
@Repository
public interface WatchlistGroupRepository extends JpaRepository<WatchlistGroupEntity, Long> {

    /**
     * 根据用户 ID 查询所有分组（按排序号升序）
     *
     * @param userId 用户 ID
     * @return 分组列表
     */
    List<WatchlistGroupEntity> findByUserIdOrderBySortOrderAsc(Long userId);

    /**
     * 根据用户 ID 和分组 ID 查询
     *
     * @param userId 用户 ID
     * @param id     分组 ID
     * @return 分组
     */
    Optional<WatchlistGroupEntity> findByUserIdAndId(Long userId, Long id);

    /**
     * 检查分组名称是否已存在（同一用户下）
     *
     * @param userId 用户 ID
     * @param name   分组名称
     * @return 是否存在
     */
    boolean existsByUserIdAndName(Long userId, String name);

    /**
     * 统计用户分组数量
     *
     * @param userId 用户 ID
     * @return 数量
     */
    long countByUserId(Long userId);
}
