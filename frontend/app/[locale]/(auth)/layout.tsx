import type { ReactNode } from "react"

/**
 * 认证页面布局
 * 不包含侧边栏，简洁的认证界面
 */
export default function AuthLayout({
  children,
}: {
  children: ReactNode
}) {
  return <>{children}</>
}
