import { usePathname } from 'next/navigation'
import { useTranslation } from '@/lib/i18n-client'
import { useLocale } from './use-locale'

/**
 * 面包屑项接口
 */
export interface BreadcrumbItem {
    label: string
    href?: string
}

/**
 * 路由到面包屑的映射配置
 */
const routeMap: Record<string, { key: string; parent?: string }> = {
    '/': { key: 'dashboard' },
    '/investment/account': { key: 'investment.account', parent: 'investment' },
    '/investment/watchlist': { key: 'investment.watchlist', parent: 'investment' },
    '/investment/holdings': { key: 'investment.holdings', parent: 'investment' },
}

/**
 * 动态面包屑 Hook
 * 
 * 根据当前路由自动生成面包屑导航
 * 支持国际化和多级路径
 * 
 * @returns 面包屑项数组
 * 
 * @example
 * ```tsx
 * const breadcrumbs = useBreadcrumbs()
 * // 在 /zh-CN/investment/account 页面
 * // 返回: [{ label: '投资管理', href: '#' }, { label: '账号管理' }]
 * ```
 */
export function useBreadcrumbs(): BreadcrumbItem[] {
    const pathname = usePathname()
    const locale = useLocale()
    const dict = useTranslation()

    // 移除语言前缀，获取实际路径
    const path = pathname.replace(`/${locale}`, '') || '/'

    // 查找路由配置
    const routeConfig = routeMap[path]

    if (!routeConfig) {
        // 如果没有配置，返回空数组
        return []
    }

    const breadcrumbs: BreadcrumbItem[] = []

    // 如果有父级，添加父级面包屑
    if (routeConfig.parent) {
        const parentKeys = routeConfig.parent.split('.')
        let parentLabel = dict as any

        for (const key of parentKeys) {
            parentLabel = parentLabel?.[key]
        }

        if (parentLabel?.title) {
            breadcrumbs.push({
                label: parentLabel.title,
                href: '#',
            })
        }
    }

    // 添加当前页面面包屑
    const keys = routeConfig.key.split('.')
    let currentLabel = dict as any

    for (const key of keys) {
        currentLabel = currentLabel?.[key]
    }

    if (currentLabel?.title) {
        breadcrumbs.push({
            label: currentLabel.title,
        })
    } else if (typeof currentLabel === 'string') {
        breadcrumbs.push({
            label: currentLabel,
        })
    }

    return breadcrumbs
}

/**
 * 获取页面标题 Hook
 * 
 * 根据当前路由返回页面标题，用于设置 document.title
 * 
 * @returns 页面标题字符串
 * 
 * @example
 * ```tsx
 * const pageTitle = usePageTitle()
 * // 在 /zh-CN/investment/account 页面
 * // 返回: "账号管理 - 投资管理"
 * ```
 */
export function usePageTitle(): string {
    const breadcrumbs = useBreadcrumbs()
    const dict = useTranslation()

    if (breadcrumbs.length === 0) {
        return dict.dashboard.hero.title || 'Dashboard'
    }

    // 反转面包屑顺序，最具体的在前
    const titles = breadcrumbs.map(b => b.label).reverse()
    return titles.join(' - ')
}
