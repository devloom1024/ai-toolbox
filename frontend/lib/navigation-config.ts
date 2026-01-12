import {
    AudioWaveform,
    BookOpen,
    Bot,
    Command,
    Frame,
    GalleryVerticalEnd,
    Map,
    PieChart,
    Settings2,
    SquareTerminal,
    Wallet,
    type LucideIcon,
} from 'lucide-react'

/**
 * 导航项配置接口
 */
export interface NavItemConfig {
    key: string
    icon: LucideIcon
    url?: string
    items?: {
        key: string
        url: string
    }[]
}

/**
 * 项目配置接口
 */
export interface ProjectConfig {
    key: string
    icon: LucideIcon
    url: string
}

/**
 * 团队配置接口
 */
export interface TeamConfig {
    name: string
    logo: LucideIcon
    plan: string
}

/**
 * 导航配置
 * 
 * 这个配置文件定义了应用的导航结构，包括：
 * - 主导航菜单（navMain）
 * - 项目列表（projects）
 * - 团队切换器（teams）
 * 
 * 所有的文本内容都通过 key 引用国际化字典，
 * 实际显示的文本在 dictionaries/*.json 中定义
 */
export const navigationConfig = {
    /**
     * 主导航菜单
     */
    navMain: [
        {
            key: 'playground',
            icon: SquareTerminal,
            url: '#',
            items: [
                { key: 'history', url: '#' },
                { key: 'starred', url: '#' },
                { key: 'settings', url: '#' },
            ],
        },
        {
            key: 'models',
            icon: Bot,
            url: '#',
            items: [
                { key: 'genesis', url: '#' },
                { key: 'explorer', url: '#' },
                { key: 'quantum', url: '#' },
            ],
        },
        {
            key: 'documentation',
            icon: BookOpen,
            url: '#',
            items: [
                { key: 'introduction', url: '#' },
                { key: 'getStarted', url: '#' },
                { key: 'tutorials', url: '#' },
                { key: 'changelog', url: '#' },
            ],
        },
        {
            key: 'investment',
            icon: Wallet,
            url: '#',
            items: [
                { key: 'account', url: '/investment/account' },
                { key: 'watchlist', url: '#' },
                { key: 'holdings', url: '#' },
            ],
        },
        {
            key: 'settings',
            icon: Settings2,
            url: '#',
            items: [
                { key: 'general', url: '#' },
                { key: 'team', url: '#' },
                { key: 'billing', url: '#' },
                { key: 'limits', url: '#' },
            ],
        },
    ] as NavItemConfig[],

    /**
     * 项目列表
     */
    projects: [
        {
            key: 'designEngineering',
            icon: Frame,
            url: '#',
        },
        {
            key: 'salesMarketing',
            icon: PieChart,
            url: '#',
        },
        {
            key: 'travel',
            icon: Map,
            url: '#',
        },
    ] as ProjectConfig[],

    /**
     * 团队列表
     */
    teams: [
        {
            name: 'Acme Inc',
            logo: GalleryVerticalEnd,
            plan: 'Enterprise',
        },
        {
            name: 'Acme Corp.',
            logo: AudioWaveform,
            plan: 'Startup',
        },
        {
            name: 'Evil Corp.',
            logo: Command,
            plan: 'Free',
        },
    ] as TeamConfig[],
}
