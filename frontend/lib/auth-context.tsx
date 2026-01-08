'use client'

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authApi, TokenResponse, ProfileResponse } from './api/auth'

/**
 * 认证状态
 */
interface AuthState {
  isAuthenticated: boolean
  isLoading: boolean
  user: ProfileResponse | null
  accessToken: string | null
  refreshToken: string | null
}

/**
 * 认证上下文
 */
interface AuthContextValue extends AuthState {
  login: (accessToken: string, refreshToken: string) => Promise<void>
  logout: () => Promise<void>
  refreshAuth: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

/**
 * Token 存储键
 */
const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'

/**
 * 认证提供者组件
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: false,
    isLoading: true,
    user: null,
    accessToken: null,
    refreshToken: null,
  })

  /**
   * 加载用户资料
   */
  const loadProfile = useCallback(async (token: string) => {
    try {
      const response = await authApi.getProfile(token)
      if (response.code === 0 && response.data) {
        setState(prev => ({
          ...prev,
          isAuthenticated: true,
          user: response.data,
        }))
      }
    } catch (error) {
      console.error('Failed to load profile:', error)
      // 如果加载失败，清除 token
      localStorage.removeItem(ACCESS_TOKEN_KEY)
      localStorage.removeItem(REFRESH_TOKEN_KEY)
      setState(prev => ({
        ...prev,
        isAuthenticated: false,
        accessToken: null,
        refreshToken: null,
        user: null,
      }))
    }
  }, [])

  /**
   * 初始化认证状态
   */
  useEffect(() => {
    const initAuth = async () => {
      const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY)
      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

      if (accessToken && refreshToken) {
        setState(prev => ({
          ...prev,
          accessToken,
          refreshToken,
        }))
        await loadProfile(accessToken)
      }

      setState(prev => ({ ...prev, isLoading: false }))
    }

    initAuth()
  }, [loadProfile])

  /**
   * 登录
   */
  const login = useCallback(async (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)

    setState(prev => ({
      ...prev,
      accessToken,
      refreshToken,
    }))

    await loadProfile(accessToken)
  }, [loadProfile])

  /**
   * 登出
   */
  const logout = useCallback(async () => {
    try {
      if (state.accessToken) {
        await authApi.logout(state.accessToken)
      }
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      localStorage.removeItem(ACCESS_TOKEN_KEY)
      localStorage.removeItem(REFRESH_TOKEN_KEY)

      setState({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        accessToken: null,
        refreshToken: null,
      })
    }
  }, [state.accessToken])

  /**
   * 刷新认证
   */
  const refreshAuth = useCallback(async () => {
    if (!state.refreshToken) {
      throw new Error('No refresh token available')
    }

    try {
      const response = await authApi.refreshToken(state.refreshToken)
      if (response.code === 0 && response.data) {
        await login(response.data.accessToken, response.data.refreshToken)
      }
    } catch (error) {
      console.error('Failed to refresh token:', error)
      await logout()
      throw error
    }
  }, [state.refreshToken, login, logout])

  const value: AuthContextValue = {
    ...state,
    login,
    logout,
    refreshAuth,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/**
 * 使用认证上下文的 Hook
 */
export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
