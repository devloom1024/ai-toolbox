# Next.js 最佳实践

## 项目配置

- **Next.js**: 16.1.1
- **React**: 19.2.3
- **TypeScript**: 5.x
- **包管理器**: pnpm 10.19.0
- **样式**: Tailwind CSS 4
- **组件库**: Radix UI + shadcn/ui

## 项目结构

```
frontend/
├── app/[locale]/                # 国际化路由
│   ├── (auth)/                  # 认证页面组
│   │   ├── login/
│   │   ├── register/
│   │   └── forgot-password/
│   ├── (app)/                   # 应用页面组
│   │   ├── page.tsx             # 仪表盘
│   │   └── layout.tsx           # 侧边栏布局
│   └── oauth/linuxdo/callback/  # OAuth 回调
├── components/
│   ├── ui/                      # shadcn/ui 基础组件
│   ├── *.tsx                    # 业务组件
│   └── *.ts                     # 工具组件
├── lib/
│   ├── api-client.ts            # axios 实例 + 拦截器
│   ├── api/*.ts                 # API 封装
│   ├── auth-context.tsx         # 认证上下文
│   ├── i18n-client.tsx          # 国际化客户端
│   └── dictionaries.ts          # 字典加载
├── hooks/
│   ├── use-locale.ts            # 获取当前语言
│   └── use-*.ts                 # 自定义 Hooks
├── dictionaries/
│   ├── en-US.json
│   └── zh-CN.json
└── next.config.ts
```

## 核心规范

### 页面组件
```typescript
'use client'

import { useTranslation } from '@/lib/i18n-client'

export default function Page() {
  const dict = useTranslation()

  return (
    <div>
      <h1>{dict.page.title}</h1>
    </div>
  )
}
```

### API 调用
```typescript
// lib/api/auth.ts
import { request } from '@/lib/api-client'

export const authApi = {
  login: (data: LoginRequest) =>
    request<TokenResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: data,
    }),
}

// 组件中使用
const response = await authApi.login(data)
```

### 认证保护
```typescript
import { AuthGuard } from '@/components/auth-guard'

export default function ProtectedPage() {
  return (
    <AuthGuard>
      <ProtectedContent />
    </AuthGuard>
  )
}
```

### 路由导航
```typescript
'use client'
import { useLocale } from '@/hooks/use-locale'
import { useRouter } from 'next/navigation'

const locale = useLocale()
const router = useRouter()

// 保持语言前缀
router.push(`/${locale}/dashboard`)
```

## 静态导出配置

```typescript
// next.config.ts
{
  output: 'export',
  trailingSlash: true,
  images: { unoptimized: true },
}
```

## 国际化

```typescript
// dictionaries/zh-CN.json
{
  "auth": {
    "login": {
      "title": "登录",
      "submit": "登录"
    }
  }
}

// 组件使用
const dict = useTranslation()
dict.auth.login.title  // "登录"
```

## 组件添加

```bash
# 添加 shadcn/ui 组件
pnpm dlx shadcn@latest add dialog button input
```

## 禁止事项

- ❌ 直接使用 axios (使用封装的 `authApi`)
- ❌ 硬编码文本 (使用 `useTranslation()`)
- ❌ 相对路径导航 (丢失语言前缀)
- ❌ 存储敏感信息到 localStorage
- ❌ 使用 Server Component 作为页面入口

## 开发命令

```bash
pnpm install              # 安装依赖
pnpm dev                  # 开发服务器
pnpm build                # 静态导出
pnpm lint                 # ESLint 检查
pnpm lint --fix           # 自动修复
```
