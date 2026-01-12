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
    '/investment/account': { key: 'investment.account', parent: 'nav.investment' },
    '/investment/watchlist': { key: 'investment.watchlist', parent: 'nav.investment' },
    '/investment/holdings': { key: 'investment.holdings', parent: 'nav.investment' },
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
    let path = pathname.replace(`/${locale}`, '') || '/'
    // 移除末尾的斜杠（除了根路径）
    if (path !== '/' && path.endsWith('/')) {
        path = path.slice(0, -1)
    }

    console.log('🍞 [Breadcrumbs] ===== 开始生成面包屑 =====')
    console.log('🍞 [Breadcrumbs] 原始路径:', pathname)
    console.log('🍞 [Breadcrumbs] 语言:', locale)
    console.log('🍞 [Breadcrumbs] 处理后路径:', path)

    // 查找路由配置
    const routeConfig = routeMap[path]
    console.log('🍞 [Breadcrumbs] 路由配置:', routeConfig)
    console.log('🍞 [Breadcrumbs] 所有路由映射:', routeMap)

    if (!routeConfig) {
        console.log('🍞 [Breadcrumbs] ❌ 未找到路由配置，返回空数组')
        return []
    }

    const breadcrumbs: BreadcrumbItem[] = []

    // 如果有父级，添加父级面包屑
    if (routeConfig.parent) {
        console.log('🍞 [Breadcrumbs] 父级配置:', routeConfig.parent)
        const parentKeys = routeConfig.parent.split('.')
        console.log('🍞 [Breadcrumbs] 父级键路径:', parentKeys)

        let parentLabel = dict as any
        for (const key of parentKeys) {
            console.log(`🍞 [Breadcrumbs] 查找父级键 "${key}":`, parentLabel?.[key])
            parentLabel = parentLabel?.[key]
        }

        console.log('🍞 [Breadcrumbs] 父级标签对象:', parentLabel)
        console.log('🍞 [Breadcrumbs] 父级标题:', parentLabel?.title)

        if (parentLabel?.title) {
            breadcrumbs.push({
                label: parentLabel.title,
                href: '#',
            })
            console.log('🍞 [Breadcrumbs] ✅ 添加父级面包屑:', parentLabel.title)
        } else {
            console.log('🍞 [Breadcrumbs] ⚠️ 父级标题不存在')
        }
    }

    // 添加当前页面面包屑
    console.log('🍞 [Breadcrumbs] 当前页配置:', routeConfig.key)
    const keys = routeConfig.key.split('.')
    console.log('🍞 [Breadcrumbs] 当前页键路径:', keys)

    let currentLabel = dict as any
    for (const key of keys) {
        console.log(`🍞 [Breadcrumbs] 查找当前页键 "${key}":`, currentLabel?.[key])
        currentLabel = currentLabel?.[key]
    }

    console.log('🍞 [Breadcrumbs] 当前页标签对象:', currentLabel)
    console.log('🍞 [Breadcrumbs] 当前页标题:', currentLabel?.title)

    if (currentLabel?.title) {
        breadcrumbs.push({
            label: currentLabel.title,
        })
        console.log('🍞 [Breadcrumbs] ✅ 添加当前页面包屑:', currentLabel.title)
    } else if (typeof currentLabel === 'string') {
        breadcrumbs.push({
            label: currentLabel,
        })
        console.log('🍞 [Breadcrumbs] ✅ 添加当前页面包屑(字符串):', currentLabel)
    } else {
        console.log('🍞 [Breadcrumbs] ⚠️ 当前页标题不存在')
    }

    console.log('🍞 [Breadcrumbs] 最终面包屑数组:', breadcrumbs)
    console.log('🍞 [Breadcrumbs] ===== 面包屑生成完成 =====')

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
