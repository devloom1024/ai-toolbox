import axios from 'axios'
import type { ErrorHandlerConfig } from './error-handler'
import { showApiError } from './error-handler'

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

apiClient.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      config.headers['X-Device-Id'] = getDeviceId()
      config.headers['Accept-Language'] = getLanguage()
      if (!config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/json'
      }
      const token = localStorage.getItem('access_token')
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const config = error.config as any
    const errorHandler = config?.errorHandler

    if (error.response) {
      const message = error.response.data?.message || error.response.statusText || '请求失败'
      const err = new Error(message) as Error & { status: number; statusText: string; url: string }
      err.status = error.response.status
      err.statusText = error.response.statusText
      err.url = error.config?.url || ''

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

export async function request<T>(
  endpoint: string,
  options: RequestInit & { errorHandler?: ErrorHandlerConfig } = {}
): Promise<ApiResponse<T>> {
  const url = getApiUrl(endpoint)
  const method = options.method || 'POST'
  const { errorHandler, ...requestOptions } = options

  const config = { errorHandler } as any

  let response
  if (method.toUpperCase() === 'GET') {
    response = await apiClient.get<ApiResponse<T>>(url, config)
  } else if (method.toUpperCase() === 'POST') {
    response = await apiClient.post<ApiResponse<T>>(url, requestOptions.body, config)
  } else if (method.toUpperCase() === 'PUT') {
    response = await apiClient.put<ApiResponse<T>>(url, requestOptions.body, config)
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
