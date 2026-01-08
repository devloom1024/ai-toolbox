import type { ReactNode } from "react"
import { AppSidebar } from "@/components/app-sidebar"
import { AuthGuard } from "@/components/auth-guard"
import {
  SidebarInset,
  SidebarProvider,
} from "@/components/ui/sidebar"

/**
 * 应用主布局
 * 包含认证保护和侧边栏布局
 * 所有需要登录的功能页面都使用此布局
 */
export default function AppLayout({
  children,
}: {
  children: ReactNode
}) {
  return (
    <AuthGuard>
      <SidebarProvider>
        <AppSidebar />
        <SidebarInset>
          {children}
        </SidebarInset>
      </SidebarProvider>
    </AuthGuard>
  )
}
