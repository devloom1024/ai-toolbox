/**
 * 主页面组件
 * 客户端组件 - 使用 SWR 从 SpringBoot API 获取数据
 */

"use client";

import useSWR from "swr";
import { AppSidebar } from "@/components/app-sidebar";
import { LanguageToggle } from "@/components/language-toggle";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Separator } from "@/components/ui/separator";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";
import { ThemeToggle } from "@/components/theme-toggle";
import { useTranslation } from "@/lib/i18n-client";
import { fetcher, getApiUrl } from "@/lib/api-client";

/**
 * 示例：定义 API 响应的类型
 */
interface DashboardData {
  // 根据实际的 SpringBoot API 响应结构定义
  // 这里是示例类型
  message?: string;
  data?: unknown;
}

export default function Page() {
  // 使用翻译 Hook（从 I18nProvider 获取）
  const dictionary = useTranslation();

  // 使用 SWR 获取数据（示例）
  // 取消注释下面的代码以启用 API 调用
  /*
  const { data, error, isLoading } = useSWR<DashboardData>(
    getApiUrl("/api/dashboard"),
    fetcher
  );

  // 处理加载状态
  if (isLoading) {
    return <div>Loading...</div>;
  }

  // 处理错误状态
  if (error) {
    return <div>Error loading data: {error.message}</div>;
  }
  */

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12">
          <div className="flex flex-1 items-center gap-2">
            <SidebarTrigger className="-ml-1" />
            <Separator
              orientation="vertical"
              className="mr-2 data-[orientation=vertical]:h-4"
            />
            <Breadcrumb>
              <BreadcrumbList>
                <BreadcrumbItem className="hidden md:block">
                  <BreadcrumbLink href="#">
                    {dictionary.header.breadcrumb.root}
                  </BreadcrumbLink>
                </BreadcrumbItem>
                <BreadcrumbSeparator className="hidden md:block" />
                <BreadcrumbItem>
                  <BreadcrumbPage>
                    {dictionary.header.breadcrumb.current}
                  </BreadcrumbPage>
                </BreadcrumbItem>
              </BreadcrumbList>
            </Breadcrumb>
          </div>
          <div className="ml-auto flex items-center gap-2">
            <LanguageToggle
              label={dictionary.header.actions.language.menu}
              srLabel={dictionary.header.actions.language.sr}
            />
            <ThemeToggle
              label={dictionary.header.actions.theme.menu}
              srLabel={dictionary.header.actions.theme.sr}
              options={dictionary.header.actions.theme.options}
            />
          </div>
        </header>
        <div className="flex flex-1 flex-col gap-6 p-6">
          <div className="rounded-xl border bg-card p-8">
            <h1 className="text-2xl font-semibold text-card-foreground">
              {dictionary.dashboard.hero.title}
            </h1>
            <p className="mt-2 text-sm text-muted-foreground">
              {dictionary.dashboard.hero.description}
            </p>
          </div>
          <div className="grid auto-rows-min gap-6 md:grid-cols-3">
            <div className="bg-muted/50 aspect-video rounded-xl" />
            <div className="bg-muted/50 aspect-video rounded-xl" />
            <div className="bg-muted/50 aspect-video rounded-xl" />
          </div>
          <div className="bg-muted/50 min-h-[100vh] flex-1 rounded-xl p-6 md:min-h-min" />
        </div>
      </SidebarInset>
    </SidebarProvider>
  )
}
