
import { request } from '../api-client'
import { PageResponse } from './common'

export enum Market {
    A_SHARE = 'A_SHARE',
    HK = 'HK',
    US = 'US',
    ETF = 'ETF',
    FUND = 'FUND'
}

export enum Recommendation {
    BUILD_POSITION = 'BUILD_POSITION',
    HOLD = 'HOLD',
    WAIT = 'WAIT',
    AVOID = 'AVOID'
}

export interface WatchlistGroup {
    id: number
    name: string
    sortOrder: number
    itemCount: number
    createdAt: string
}

export interface WatchlistGroupCreateRequest {
    name: string
}

export interface WatchlistItem {
    id: number
    symbol: string
    market: Market
    name: string
    currentPrice: number
    changePercent: number
    recommendation: Recommendation
    confidence: number
    groupId?: number
    groupName?: string
    addedAt: string
}

export type WatchlistResponse = PageResponse<WatchlistItem>

export interface WatchlistAddRequest {
    symbol: string
    market: Market
    name?: string
    groupId?: number
}

export interface StockSearchResult {
    symbol: string
    name: string
    market: Market
    currentPrice?: number
}

export const watchlistApi = {
    /**
     * Get watchlist items
     */
    getWatchlist: (params?: { groupId?: number; market?: Market; page?: number; pageSize?: number }) => {
        return request<WatchlistResponse>({
            url: '/api/v1/investment/watchlist',
            method: 'GET',
            params
        })
    },

    /**
     * Add to watchlist
     */
    addToWatchlist: (data: WatchlistAddRequest) => {
        return request<void>({
            url: '/api/v1/investment/watchlist',
            method: 'POST',
            data
        })
    },

    /**
     * Remove from watchlist
     */
    removeFromWatchlist: (id: number) => {
        return request<void>({
            url: `/api/v1/investment/watchlist/${id}`,
            method: 'DELETE'
        })
    },

    /**
     * Get watchlist groups
     */
    getGroups: () => {
        return request<WatchlistGroup[]>({
            url: '/api/v1/investment/watchlist/group',
            method: 'GET'
        })
    },

    /**
     * Create watchlist group
     */
    createGroup: (data: WatchlistGroupCreateRequest) => {
        return request<void>({
            url: '/api/v1/investment/watchlist/group',
            method: 'POST',
            data
        })
    },

    /**
     * Search stocks
     */
    searchStocks: (keyword: string, market?: Market) => {
        return request<StockSearchResult[]>({
            url: '/api/v1/investment/watchlist/search',
            method: 'GET',
            params: { keyword, market }
        })
    }
}
