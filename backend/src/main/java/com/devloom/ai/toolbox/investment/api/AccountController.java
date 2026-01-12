package com.devloom.ai.toolbox.investment.api;

import com.devloom.ai.toolbox.common.response.ApiResponse;
import com.devloom.ai.toolbox.investment.dto.request.AccountCreateRequest;
import com.devloom.ai.toolbox.investment.dto.request.AccountUpdateRequest;
import com.devloom.ai.toolbox.investment.dto.response.AccountResponse;
import com.devloom.ai.toolbox.investment.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投资账号管理 API
 *
 * @author huangkl
 */
@RestController
@RequestMapping("/api/v1/investment/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取投资账号列表
     *
     * @param includeHidden 是否包含隐藏账号
     * @return 账号列表
     */
    @GetMapping
    public ApiResponse<List<AccountResponse>> getAccountList(
            @RequestParam(required = false, defaultValue = "false") Boolean includeHidden) {
        log.debug("获取账号列表，includeHidden={}", includeHidden);
        List<AccountResponse> accounts = accountService.getAccountList(includeHidden);
        return ApiResponse.success(accounts);
    }

    /**
     * 获取账号详情
     *
     * @param id 账号 ID
     * @return 账号详情
     */
    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable Long id) {
        log.debug("获取账号详情，id={}", id);
        AccountResponse account = accountService.getAccountById(id);
        return ApiResponse.success(account);
    }

    /**
     * 添加投资账号
     *
     * @param request 创建请求
     * @return 成功响应
     */
    @PostMapping
    public ApiResponse<Void> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        log.info("创建账号，request={}", request);
        accountService.createAccount(request);
        return ApiResponse.success();
    }

    /**
     * 更新账号
     *
     * @param id      账号 ID
     * @param request 更新请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request) {
        log.info("更新账号，id={}, request={}", id, request);
        accountService.updateAccount(id, request);
        return ApiResponse.success();
    }

    /**
     * 删除账号
     *
     * @param id 账号 ID（删除前需先删除关联的持仓）
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAccount(@PathVariable Long id) {
        log.info("删除账号，id={}", id);
        accountService.deleteAccount(id);
        return ApiResponse.success();
    }
}
