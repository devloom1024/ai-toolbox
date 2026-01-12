package com.devloom.ai.toolbox.investment.service;

import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import com.devloom.ai.toolbox.common.security.CurrentUserHolder;
import com.devloom.ai.toolbox.investment.domain.entity.AccountEntity;
import com.devloom.ai.toolbox.investment.domain.repository.AccountRepository;
import com.devloom.ai.toolbox.investment.dto.request.AccountCreateRequest;
import com.devloom.ai.toolbox.investment.dto.request.AccountUpdateRequest;
import com.devloom.ai.toolbox.investment.dto.response.AccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投资账号服务
 *
 * @author huangkl
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * 获取账号列表
     *
     * @param includeHidden 是否包含隐藏账号
     * @return 账号列表
     */
    public List<AccountResponse> getAccountList(Boolean includeHidden) {
        Long userId = CurrentUserHolder.getUserId();
        log.debug("获取用户 {} 的账号列表，includeHidden={}", userId, includeHidden);

        List<AccountEntity> accounts;
        if (Boolean.TRUE.equals(includeHidden)) {
            accounts = accountRepository.findByUserIdOrderBySortOrderAsc(userId);
        } else {
            accounts = accountRepository.findByUserIdAndIsHiddenOrderBySortOrderAsc(userId, false);
        }

        return accounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取账号详情
     *
     * @param accountId 账号 ID
     * @return 账号详情
     */
    public AccountResponse getAccountById(Long accountId) {
        Long userId = CurrentUserHolder.getUserId();
        log.debug("获取用户 {} 的账号详情，accountId={}", userId, accountId);

        AccountEntity account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        return convertToResponse(account);
    }

    /**
     * 创建账号
     *
     * @param request 创建请求
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void createAccount(AccountCreateRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        log.info("用户 {} 创建账号，accountType={}, accountName={}",
                userId, request.getAccountType(), request.getAccountName());

        AccountEntity account = AccountEntity.builder()
                .userId(userId)
                .accountType(request.getAccountType())
                .accountName(request.getAccountName())
                .accountIcon(request.getAccountIcon() != null ? request.getAccountIcon() : "")
                .isActive(true)
                .isHidden(false)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : (short) 0)
                .build();

        accountRepository.save(account);
        log.info("账号创建成功，accountId={}", account.getId());
    }

    /**
     * 更新账号
     *
     * @param accountId 账号 ID
     * @param request   更新请求
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateAccount(Long accountId, AccountUpdateRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        log.info("用户 {} 更新账号，accountId={}", userId, accountId);

        AccountEntity account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        // 更新字段（仅更新非 null 字段）
        if (request.getAccountName() != null) {
            account.setAccountName(request.getAccountName());
        }
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        if (request.getAccountIcon() != null) {
            account.setAccountIcon(request.getAccountIcon());
        }
        if (request.getIsActive() != null) {
            account.setIsActive(request.getIsActive());
        }
        if (request.getIsHidden() != null) {
            account.setIsHidden(request.getIsHidden());
        }
        if (request.getSortOrder() != null) {
            account.setSortOrder(request.getSortOrder());
        }

        accountRepository.save(account);
        log.info("账号更新成功，accountId={}", accountId);
    }

    /**
     * 删除账号
     *
     * @param accountId 账号 ID
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteAccount(Long accountId) {
        Long userId = CurrentUserHolder.getUserId();
        log.info("用户 {} 删除账号，accountId={}", userId, accountId);

        AccountEntity account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        // TODO: 检查是否有关联的持仓，如果有则不允许删除
        // 这里暂时直接删除，后续实现持仓管理时再添加检查逻辑

        accountRepository.delete(account);
        log.info("账号删除成功，accountId={}", accountId);
    }

    /**
     * 转换为响应 DTO
     *
     * @param account 账号实体
     * @return 响应 DTO
     */
    private AccountResponse convertToResponse(AccountEntity account) {
        // TODO: 查询持仓数量和总资产
        // 这里暂时返回默认值，后续实现持仓管理时再完善

        return AccountResponse.builder()
                .id(account.getId())
                .accountType(account.getAccountType())
                .accountName(account.getAccountName())
                .accountIcon(account.getAccountIcon())
                .isActive(account.getIsActive())
                .isHidden(account.getIsHidden())
                .sortOrder(account.getSortOrder())
                .positionCount(0)
                .totalAssets(BigDecimal.ZERO)
                .createdAt(account.getCreatedAt())
                .build();
    }
}
