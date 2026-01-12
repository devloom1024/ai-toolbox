import axios from 'axios'
import type { ErrorHandlerConfig } from './error-handler'
import { showApiError } from './error-handler'
import { authApi } from './api/auth'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T | null
  traceId: string | null
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

function getDeviceId(): string {
  if (typeof window === 'undefined') {
    return 'server-side-render'
  }
  const DEVICE_ID_KEY = 'device-id'
  let deviceId = localStorage.getItem(DEVICE_ID_KEY)
  if (!deviceId) {
    deviceId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0
      const v = c === 'x' ? r : (r & 0x3) | 0x8
      return v.toString(16)
    })
    localStorage.setItem(DEVICE_ID_KEY, deviceId)
  }
  return deviceId
}

function getLanguage(): string {
  if (typeof window === 'undefined') {
    return 'en-US'
  }
  const pathname = window.location.pathname
  const segments = pathname.split('/').filter(Boolean)
  const locale = segments[0]
  if (locale && (locale === 'zh-CN' || locale === 'en-US')) {
    return locale
  }
  return 'en-US'
}

const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'

let isRefreshing = false
let failedRequestsQueue: Array<{
  resolve: (token: string) => void
  reject: (error: Error) => void
}> = []

function processQueue(token?: string, error?: Error) {
  failedRequestsQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token!)
    }
  })
  failedRequestsQueue = []
}

async function doRefreshToken(): Promise<{ accessToken: string; refreshToken: string }> {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
  if (!refreshToken) {
    throw new Error('No refresh token available')
  }
  const response = await authApi.refreshToken(refreshToken)
  if (response.code !== 0 || !response.data) {
    throw new Error(response.message || 'Token refresh failed')
  }
  return response.data
}

function clearAuthData() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

apiClient.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      config.headers['X-Device-Id'] = getDeviceId()
      config.headers['Accept-Language'] = getLanguage()
      if (!config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/json'
      }
      // 刷新 token 接口不应该携带 Authorization header，避免过期 token 导致 401
      const isRefreshEndpoint = config.url?.includes('/api/v1/auth/token/refresh')
      if (!isRefreshEndpoint) {
        const token = localStorage.getItem('access_token')
        if (token) {
          config.headers['Authorization'] = `Bearer ${token}`
        }
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

interface ExtendedAxiosConfig {
  _retry?: boolean
  errorHandler?: ErrorHandlerConfig
  headers?: Record<string, string>
  url?: string
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as ExtendedAxiosConfig
    const errorHandler = originalRequest?.errorHandler

    if (error.response) {
      const status = error.response.status

      if (status === 401 && !originalRequest._retry) {
        // 如果是刷新 token 的接口报 401，说明 refresh token 也失效了，直接跳转登录
        const isRefreshEndpoint = originalRequest.url?.includes('/api/v1/auth/token/refresh')
        if (isRefreshEndpoint) {
          clearAuthData()
          if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
            window.location.href = `/${getLanguage()}/login?reason=session_expired`
          }
          return Promise.reject(error)
        }

        originalRequest._retry = true

        if (!isRefreshing) {
          isRefreshing = true

          try {
            const tokens = await doRefreshToken()
            localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken)
            localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken)

            apiClient.defaults.headers.common['Authorization'] = `Bearer ${tokens.accessToken}`
            if (originalRequest.headers) {
              originalRequest.headers['Authorization'] = `Bearer ${tokens.accessToken}`
            }

            processQueue(tokens.accessToken)

            return apiClient(originalRequest)
          } catch (refreshError) {
            processQueue(undefined, refreshError as Error)
            clearAuthData()

            if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
              window.location.href = `/${getLanguage()}/login?reason=session_expired`
            }

            return Promise.reject(refreshError)
          } finally {
            isRefreshing = false
          }
        }

        return new Promise((resolve, reject) => {
          failedRequestsQueue.push({
            resolve: (token: string) => {
              if (originalRequest.headers) {
                originalRequest.headers['Authorization'] = `Bearer ${token}`
              }
              resolve(apiClient(originalRequest))
            },
            reject: (err: Error) => {
              reject(err)
            },
          })
        })
      }

      const message = error.response.data?.message || error.response.statusText || '请求失败'
      const err = new Error(message) as Error & { status: number; statusText: string; url: string }
      err.status = status
      err.statusText = error.response.statusText
      err.url = originalRequest?.url || ''

      if (!errorHandler?.showToast) {
        return Promise.reject(err)
      }
      showApiError(message, errorHandler)
      return Promise.reject(err)
    }
    if (error.request) {
      if (!errorHandler?.showToast) {
        return Promise.reject(new Error('网络请求失败'))
      }
      showApiError('网络请求失败', errorHandler)
      return Promise.reject(new Error('网络请求失败'))
    }
    return Promise.reject(error)
  }
)

type AxiosRequestConfig<D = any> = {
  errorHandler?: ErrorHandlerConfig
} & D

export async function fetcher<T = unknown>(url: string): Promise<T> {
  const response = await apiClient.get<T>(url)
  return response.data
}

export function getApiUrl(endpoint: string): string {
  const normalizedEndpoint = endpoint.startsWith("/") ? endpoint : `/${endpoint}`
  return `${API_BASE_URL}${normalizedEndpoint}`
}

// 函数重载：支持两种调用方式
export async function request<T>(
  endpointOrConfig: string | {
    url: string
    method?: string
    data?: any
    params?: any
    errorHandler?: ErrorHandlerConfig
  },
  options?: RequestInit & { errorHandler?: ErrorHandlerConfig }
): Promise<ApiResponse<T>> {
  let url: string
  let method: string
  let errorHandler: ErrorHandlerConfig | undefined
  let requestData: any
  let requestParams: any

  // 判断是对象参数还是字符串参数
  if (typeof endpointOrConfig === 'string') {
    // 传统方式: request('/api/path', { method: 'GET', body: ... })
    url = getApiUrl(endpointOrConfig)
    method = options?.method || 'POST'
    errorHandler = options?.errorHandler
    requestData = (options as any)?.body
  } else {
    // 新方式: request({ url: '/api/path', method: 'GET', data: ... })
    url = getApiUrl(endpointOrConfig.url)
    method = endpointOrConfig.method || 'GET'
    errorHandler = endpointOrConfig.errorHandler
    requestData = endpointOrConfig.data
    requestParams = endpointOrConfig.params
  }

  const config: any = { errorHandler }

  // 如果有 params，添加到 config 中（用于 GET 请求的查询参数）
  if (requestParams) {
    config.params = requestParams
  }

  let response
  if (method.toUpperCase() === 'GET') {
    response = await apiClient.get<ApiResponse<T>>(url, config)
  } else if (method.toUpperCase() === 'POST') {
    response = await apiClient.post<ApiResponse<T>>(url, requestData, config)
  } else if (method.toUpperCase() === 'PUT') {
    response = await apiClient.put<ApiResponse<T>>(url, requestData, config)
  } else if (method.toUpperCase() === 'DELETE') {
    response = await apiClient.delete<ApiResponse<T>>(url, config)
  } else {
    throw new Error(`Unsupported HTTP method: ${method}`)
  }

  const data = response.data

  if (data.code !== 0) {
    const error = new Error(data.message || 'Request failed') as Error & { code: number }
    error.code = data.code
    if (!errorHandler?.showToast) {
      throw error
    }
    showApiError(data.message || '请求失败', errorHandler)
    throw error
  }

  return data
}

export async function post<T = unknown, D = unknown>(
  endpoint: string,
  data: D,
  errorHandler?: ErrorHandlerConfig
): Promise<T> {
  const response = await apiClient.post<T>(endpoint, data, { errorHandler } as any)
  return response.data
}

export async function put<T = unknown, D = unknown>(
  endpoint: string,
  data: D,
  errorHandler?: ErrorHandlerConfig
): Promise<T> {
  const response = await apiClient.put<T>(endpoint, data, { errorHandler } as any)
  return response.data
}

export async function del<T = unknown>(
  endpoint: string,
  errorHandler?: ErrorHandlerConfig
): Promise<T> {
  const response = await apiClient.delete<T>(endpoint, { errorHandler } as any)
  return response.data
}

export { apiClient }
