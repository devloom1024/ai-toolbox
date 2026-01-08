import { request, apiClient } from '../api-client'
import type { ApiResponse, TokenResponse } from '../api-client'
import type { ErrorHandlerConfig } from '../error-handler'

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
 * 认证 API 服务
 */
export const authApi = {
  /**
   * 用户注册
   */
  register: (data: RegisterRequest, errorHandler?: ErrorHandlerConfig) => {
    return request<RegisterResponse>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
      errorHandler,
    })
  },

  /**
   * 用户登录
   */
  login: (data: LoginRequest, errorHandler?: ErrorHandlerConfig) => {
    return request<TokenResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
      errorHandler,
    })
  },

  /**
   * 请求邮箱验证码
   */
  requestEmailCode: (data: EmailCodeRequest, errorHandler?: ErrorHandlerConfig) => {
    return request<null>('/api/v1/auth/code/email', {
      method: 'POST',
      body: JSON.stringify(data),
      errorHandler,
    })
  },

  /**
   * 刷新 Token
   */
  refreshToken: (refreshToken: string, errorHandler?: ErrorHandlerConfig) => {
    return request<TokenResponse>('/api/v1/auth/token/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
      errorHandler,
    })
  },

  /**
   * 登出
   */
  logout: (scope: 'CURRENT' | 'ALL' = 'CURRENT', errorHandler?: ErrorHandlerConfig) => {
    return request<null>('/api/v1/auth/logout', {
      method: 'POST',
      body: JSON.stringify({ scope }),
      errorHandler,
    })
  },

  /**
   * 重置密码
   */
  resetPassword: (data: PasswordResetRequest, errorHandler?: ErrorHandlerConfig) => {
    return request<null>('/api/v1/auth/password/reset', {
      method: 'POST',
      body: JSON.stringify(data),
      errorHandler,
    })
  },

  /**
   * 获取用户资料
   */
  getProfile: (errorHandler?: ErrorHandlerConfig) => {
    return request<ProfileResponse>('/api/v1/auth/profile', {
      method: 'GET',
      errorHandler,
    })
  },
}

export type { ApiResponse, TokenResponse }
