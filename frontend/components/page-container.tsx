'use client'

import { ReactNode, useEffect } from 'react'
import { LanguageToggle } from '@/components/language-toggle'
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from '@/components/ui/breadcrumb'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import { ThemeToggle } from '@/components/theme-toggle'
import { useTranslation } from '@/lib/i18n-client'
import { useBreadcrumbs, usePageTitle } from '@/hooks/use-breadcrumbs'

interface BreadcrumbItem {
    label: string
    href?: string
}

interface PageContainerProps {
    /**
     * 面包屑配置
     * - 如果不传，自动根据路由生成
     * - 如果传入，使用自定义面包屑
     * - 如果传入空数组，不显示面包屑
     */
    breadcrumbs?: BreadcrumbItem[]
    /**
     * 页面标题（用于 document.title）
     * - 如果不传，自动根据面包屑生成
     */
    pageTitle?: string
    children: ReactNode
}

/**
 * 通用页面容器组件
 * 
 * 提供统一的页面布局，包括：
 * - 顶部栏（侧边栏切换、面包屑、语言切换、主题切换）
 * - 主内容区
 * - 自动面包屑生成
 * - 页面标题设置
 * 
 * @example
 * ```tsx
 * // 自动面包屑（推荐）
 * <PageContainer>
 *   <YourContent />
 * </PageContainer>
 * 
 * // 自定义面包屑
 * <PageContainer breadcrumbs={[
 *   { label: '投资管理', href: '#' },
 *   { label: '账号管理' }
 * ]}>
 *   <YourContent />
 * </PageContainer>
 * 
 * // 不显示面包屑
 * <PageContainer breadcrumbs={[]}>
 *   <YourContent />
 * </PageContainer>
 * ```
 */
export function PageContainer({ breadcrumbs: customBreadcrumbs, pageTitle, children }: PageContainerProps) {
    const dictionary = useTranslation()
    const autoBreadcrumbs = useBreadcrumbs()
    const autoPageTitle = usePageTitle()

    // 使用自定义面包屑或自动生成的面包屑
    const breadcrumbs = customBreadcrumbs !== undefined ? customBreadcrumbs : autoBreadcrumbs
    const title = pageTitle || autoPageTitle

    console.log('📄 [PageContainer] 自定义面包屑:', customBreadcrumbs)
    console.log('📄 [PageContainer] 自动面包屑:', autoBreadcrumbs)
    console.log('📄 [PageContainer] 最终使用的面包屑:', breadcrumbs)
    console.log('📄 [PageContainer] 页面标题:', title)

    // 设置页面标题
    useEffect(() => {
        if (title) {
            document.title = `${title} - AI Toolbox`
        }
    }, [title])

    return (
        <>
            <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12">
                <div className="flex flex-1 items-center gap-2">
                    <SidebarTrigger className="-ml-1" />
                    <Separator
                        orientation="vertical"
                        className="mr-2 data-[orientation=vertical]:h-4"
                    />
                    {breadcrumbs && breadcrumbs.length > 0 && (
                        <Breadcrumb>
                            <BreadcrumbList>
                                {breadcrumbs.map((item, index) => (
                                    <div key={index} className="flex items-center">
                                        {index > 0 && <BreadcrumbSeparator className="hidden md:block" />}
                                        <BreadcrumbItem className={index === 0 ? 'hidden md:block' : ''}>
                                            {item.href ? (
                                                <BreadcrumbLink href={item.href}>
                                                    {item.label}
                                                </BreadcrumbLink>
                                            ) : (
                                                <BreadcrumbPage>{item.label}</BreadcrumbPage>
                                            )}
                                        </BreadcrumbItem>
                                    </div>
                                ))}
                            </BreadcrumbList>
                        </Breadcrumb>
                    )}
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
                {children}
            </div>
        </>
    )
}
