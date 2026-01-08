/**
 * API 客户端配置
 * 用于与 SpringBoot 后端进行交互
 */

import axios from 'axios';

/**
 * API 基础 URL
 * 从环境变量读取，默认为本地开发服务器地址
 */
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

/**
 * API 响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T | null
  traceId: string | null
}

/**
 * Token 响应数据
 */
export interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

/**
 * 获取或生成设备ID
 * 设备ID用于后端识别不同的设备登录会话
 */
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

/**
 * 获取当前语言
 * 从 URL 路径中提取语言代码
 */
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

/**
 * Axios 实例
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

/**
 * 请求拦截器 - 添加认证 token 和通用 header
 */
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
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器 - 统一错误处理
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const message = error.response.data?.message || error.response.statusText || '请求失败'
      const err = new Error(message) as Error & { status: number; statusText: string; url: string }
      err.status = error.response.status
      err.statusText = error.response.statusText
      err.url = error.config?.url || ''
      return Promise.reject(err)
    }
    if (error.request) {
      return Promise.reject(new Error('网络请求失败'))
    }
    return Promise.reject(error)
  }
)

/**
 * SWR fetcher 函数
 */
export async function fetcher<T = unknown>(url: string): Promise<T> {
  const response = await apiClient.get<T>(url)
  return response.data
}

/**
 * 构建完整的 API URL
 */
export function getApiUrl(endpoint: string): string {
  const normalizedEndpoint = endpoint.startsWith("/")
    ? endpoint
    : `/${endpoint}`;
  return `${API_BASE_URL}${normalizedEndpoint}`;
}

/**
 * 通用请求方法
 */
export async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const url = getApiUrl(endpoint)
  const method = options.method || 'POST'

  let response
  if (method.toUpperCase() === 'GET') {
    response = await apiClient.get<ApiResponse<T>>(url)
  } else if (method.toUpperCase() === 'POST') {
    response = await apiClient.post<ApiResponse<T>>(url, options.body)
  } else if (method.toUpperCase() === 'PUT') {
    response = await apiClient.put<ApiResponse<T>>(url, options.body)
  } else if (method.toUpperCase() === 'DELETE') {
    response = await apiClient.delete<ApiResponse<T>>(url)
  } else {
    throw new Error(`Unsupported HTTP method: ${method}`)
  }

  const data = response.data

  if (data.code !== 0) {
    throw new Error(data.message || 'Request failed')
  }

  return data
}

/**
 * POST 请求辅助函数
 */
export async function post<T = unknown, D = unknown>(
  endpoint: string,
  data: D
): Promise<T> {
  const response = await apiClient.post<T>(endpoint, data)
  return response.data
}

/**
 * PUT 请求辅助函数
 */
export async function put<T = unknown, D = unknown>(
  endpoint: string,
  data: D
): Promise<T> {
  const response = await apiClient.put<T>(endpoint, data)
  return response.data
}

/**
 * DELETE 请求辅助函数
 */
export async function del<T = unknown>(endpoint: string): Promise<T> {
  const response = await apiClient.delete<T>(endpoint)
  return response.data
}

/**
 * Axios 实例导出
 */
export { apiClient }
