import { getApiUrl, createHeaders } from '../api-client'

/**
 * API 响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T | null
}

/**
 * 注册请求参数
 */
export interface RegisterRequest {
  email: string
  password: string
  code: string
  nickname: string
}

/**
 * 注册响应数据
 */
export interface RegisterResponse {
  token: TokenResponse
  userId: number
}

/**
 * 登录请求参数
 */
export interface LoginRequest {
  identifier: string
  password: string
  type?: 'EMAIL'
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
 * 邮箱验证码请求参数
 */
export interface EmailCodeRequest {
  email: string
  scene: 'REGISTER' | 'RESET_PASSWORD'
}

/**
 * 重置密码请求参数
 */
export interface PasswordResetRequest {
  email: string
  code: string
  newPassword: string
  confirmPassword: string
}

/**
 * 用户资料响应数据
 */
export interface ProfileResponse {
  userId: number
  nickname: string
  avatar: string
  status: number
  bindings: Array<{
    type: 'EMAIL' | 'LINUX_DO'
    identifier: string
    verified: boolean
  }>
}

/**
 * 创建带认证的请求选项
 */
function createAuthHeaders(token?: string): HeadersInit {
  const headers = createHeaders()

  if (token) {
    return {
      ...headers,
      'Authorization': `Bearer ${token}`
    }
  }

  return headers
}

/**
 * 通用请求方法
 */
async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const url = getApiUrl(endpoint)
  const response = await fetch(url, {
    ...options,
    headers: createHeaders(options.headers),
  })

  const data: ApiResponse<T> = await response.json()

  if (data.code !== 0 && !response.ok) {
    throw new Error(data.message || 'Request failed')
  }

  return data
}

/**
 * 认证 API 服务
 */
export const authApi = {
  /**
   * 用户注册
   */
  register: (data: RegisterRequest) => {
    return request<RegisterResponse>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  /**
   * 用户登录
   */
  login: (data: LoginRequest) => {
    return request<TokenResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  /**
   * 请求邮箱验证码
   */
  requestEmailCode: (data: EmailCodeRequest) => {
    return request<null>('/api/v1/auth/code/email', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  /**
   * 刷新 Token
   */
  refreshToken: (refreshToken: string) => {
    return request<TokenResponse>('/api/v1/auth/token/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    })
  },

  /**
   * 登出
   */
  logout: (token: string, scope: 'CURRENT' | 'ALL' = 'CURRENT') => {
    return request<null>('/api/v1/auth/logout', {
      method: 'POST',
      body: JSON.stringify({ scope }),
      headers: createAuthHeaders(token),
    })
  },

  /**
   * 重置密码
   */
  resetPassword: (data: PasswordResetRequest) => {
    return request<null>('/api/v1/auth/password/reset', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  /**
   * 获取用户资料
   */
  getProfile: (token: string) => {
    return request<ProfileResponse>('/api/v1/auth/profile', {
      method: 'GET',
      headers: createAuthHeaders(token),
    })
  },
}
