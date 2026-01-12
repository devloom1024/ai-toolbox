import { useLocale } from './use-locale'
import { useTranslation } from '@/lib/i18n-client'
import { navigationConfig } from '@/lib/navigation-config'
import type { LucideIcon } from 'lucide-react'

/**
 * 导航项接口（用于 NavMain 组件）
 */
export interface NavItem {
    title: string
    url: string
    icon?: LucideIcon
    isActive?: boolean
    items?: {
        title: string
        url: string
    }[]
}

/**
 * 项目接口（用于 NavProjects 组件）
 */
export interface ProjectItem {
    name: string
    url: string
    icon: LucideIcon
}

/**
 * 导航数据 Hook
 * 
 * 将导航配置转换为国际化的导航数据
 * 
 * @returns 包含主导航和项目列表的对象
 * 
 * @example
 * ```tsx
 * const { navMain, projects } = useNavigation()
 * 
 * return (
 *   <>
 *     <NavMain items={navMain} />
 *     <NavProjects projects={projects} />
 *   </>
 * )
 * ```
 */
export function useNavigation() {
    const locale = useLocale()
    const dict = useTranslation()

    // 转换主导航
    const navMain: NavItem[] = navigationConfig.navMain.map((item) => {
        const navSection = (dict.nav as any)[item.key]
        return {
            title: navSection?.title || item.key,
            url: item.url || '#',
            icon: item.icon,
            isActive: item.key === 'playground', // 可以根据当前路由动态设置
            items: item.items?.map((subItem) => ({
                title: navSection?.[subItem.key] || subItem.key,
                url: subItem.url,
            })),
        }
    })

    // 转换项目列表
    const projects: ProjectItem[] = navigationConfig.projects.map((project) => ({
        name: (dict.nav.projects as any)[project.key] || project.key,
        url: project.url,
        icon: project.icon,
    }))

    return {
        navMain,
        projects,
        teams: navigationConfig.teams, // 团队列表暂时不需要国际化
    }
}
