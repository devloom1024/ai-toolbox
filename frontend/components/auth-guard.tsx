'use client'

import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuth } from '@/lib/auth-context'

/**
 * 认证保护组件
 * 用于保护需要登录才能访问的页面
 */
export function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const { isAuthenticated, isLoading } = useAuth()

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      // 未登录，重定向到登录页
      // 从 pathname 中提取 locale（如 /zh-CN/xxx → zh-CN）
      const segments = pathname.split('/').filter(Boolean)
      const locale = segments[0] || 'zh-CN'

      // 保存当前路径，登录后可以返回
      const loginUrl = `/${locale}/login?redirect=${encodeURIComponent(pathname)}`
      router.push(loginUrl)
    }
  }, [isAuthenticated, isLoading, router, pathname])

  // 加载中显示加载状态
  if (isLoading) {
    return (
      <div className="flex min-h-svh items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <div className="border-primary h-12 w-12 animate-spin rounded-full border-4 border-t-transparent" />
          <p className="text-muted-foreground text-sm">加载中...</p>
        </div>
      </div>
    )
  }

  // 未认证时不渲染内容（即将重定向）
  if (!isAuthenticated) {
    return null
  }

  // 已认证，渲染子组件
  return <>{children}</>
}
