import { request, ApiResponse } from '../api-client'

/**
 * 账号类型枚举
 */
export enum AccountType {
    BROKER = 'BROKER',
    FUND_PLATFORM = 'FUND_PLATFORM',
    BANK = 'BANK',
    ALIPAY = 'ALIPAY',
    OTHER = 'OTHER'
}

/**
 * 账号响应
 */
export interface AccountResponse {
    id: number
    accountType: AccountType
    accountName: string
    accountIcon: string
    isActive: boolean
    isHidden: boolean
    sortOrder: number
    positionCount: number
    totalAssets: number
    createdAt: string
}

/**
 * 创建账号请求
 */
export interface AccountCreateRequest {
    accountType: AccountType
    accountName: string
    accountIcon?: string
    sortOrder?: number
}

/**
 * 更新账号请求
 */
export interface AccountUpdateRequest {
    accountName?: string
    accountType?: AccountType
    accountIcon?: string
    isActive?: boolean
    isHidden?: boolean
    sortOrder?: number
}

/**
 * 投资账号 API
 */
export const accountApi = {
    /**
     * 获取账号列表
     */
    getAccountList: (includeHidden = false) => {
        return request<AccountResponse[]>({
            url: '/api/v1/investment/account',
            method: 'GET',
            params: { includeHidden }
        })
    },

    /**
     * 获取账号详情
     */
    getAccountById: (id: number) => {
        return request<AccountResponse>({
            url: `/api/v1/investment/account/${id}`,
            method: 'GET'
        })
    },

    /**
     * 创建账号
     */
    createAccount: (data: AccountCreateRequest) => {
        return request<void>({
            url: '/api/v1/investment/account',
            method: 'POST',
            data
        })
    },

    /**
     * 更新账号
     */
    updateAccount: (id: number, data: AccountUpdateRequest) => {
        return request<void>({
            url: `/api/v1/investment/account/${id}`,
            method: 'PUT',
            data
        })
    },

    /**
     * 删除账号
     */
    deleteAccount: (id: number) => {
        return request<void>({
            url: `/api/v1/investment/account/${id}`,
            method: 'DELETE'
        })
    }
}
