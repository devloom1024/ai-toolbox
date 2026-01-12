package com.devloom.ai.toolbox.investment.domain.repository;

import com.devloom.ai.toolbox.investment.domain.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 投资账号 Repository
 *
 * @author huangkl
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    /**
     * 根据用户 ID 查询所有账号（按排序序号升序）
     *
     * @param userId 用户 ID
     * @return 账号列表
     */
    List<AccountEntity> findByUserIdOrderBySortOrderAsc(Long userId);

    /**
     * 根据用户 ID 和隐藏状态查询账号（按排序序号升序）
     *
     * @param userId   用户 ID
     * @param isHidden 是否隐藏
     * @return 账号列表
     */
    List<AccountEntity> findByUserIdAndIsHiddenOrderBySortOrderAsc(Long userId, Boolean isHidden);

    /**
     * 根据用户 ID 和账号 ID 查询账号
     *
     * @param userId    用户 ID
     * @param accountId 账号 ID
     * @return 账号
     */
    Optional<AccountEntity> findByUserIdAndId(Long userId, Long accountId);

    /**
     * 根据用户 ID 统计账号数量
     *
     * @param userId 用户 ID
     * @return 账号数量
     */
    long countByUserId(Long userId);
}
