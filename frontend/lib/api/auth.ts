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
 * LinuxDo OAuth 授权响应数据
 */
export interface LinuxDoAuthorizeResponse {
  state: string
  authorizeUrl: string
}

/**
 * LinuxDo OAuth 回调参数
 */
export interface LinuxDoCallbackParams {
  code: string
  state: string
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

  /**
   * 获取 LinuxDo OAuth 授权链接
   * @param redirectUri 回调地址（前端页面地址）
   */
  linuxDoAuthorize: (redirectUri: string, errorHandler?: ErrorHandlerConfig) => {
    const params = new URLSearchParams()
    params.set('redirect_uri', redirectUri)
    return request<LinuxDoAuthorizeResponse>(`/api/v1/auth/oauth/linuxdo/authorize?${params}`, {
      method: 'GET',
      errorHandler,
    })
  },

  /**
   * 处理 LinuxDo OAuth 回调，换取 token
   */
  linuxDoCallback: (code: string, state: string, errorHandler?: ErrorHandlerConfig) => {
    return request<TokenResponse>(`/api/v1/auth/oauth/linuxdo/callback?code=${code}&state=${state}`, {
      method: 'GET',
      errorHandler,
    })
  },

  /**
   * 绑定 LinuxDo 账号
   */
  bindLinuxDo: (data: { code: string; state: string }, errorHandler?: ErrorHandlerConfig) => {
    return request<null>('/api/v1/auth/bind/linuxdo', {
      method: 'POST',
      body: JSON.stringify(data),
      errorHandler,
    })
  },
}

export type { ApiResponse, TokenResponse, LinuxDoAuthorizeResponse, LinuxDoCallbackParams }
