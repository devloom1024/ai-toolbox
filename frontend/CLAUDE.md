# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是前端项目，基于 Next.js 16 (App Router) + React 19 + TypeScript，采用**静态导出模式**。

**技术栈**:
- Next.js 16 + React 19 + TypeScript
- Tailwind CSS 4 + shadcn/ui (Radix UI)
- axios + SWR (数据获取)
- sonner (Toast 通知)
- next-themes (主题切换)
- pnpm 10.19.0 (包管理器)

**核心特性**:
- ✅ 静态导出 - 生成纯静态文件到 `out/`，无需 Node.js 运行时
- ✅ 国际化 - 支持 en-US / zh-CN，路由级别的语言切换
- ✅ JWT 认证 - localStorage 存储 Token，自动注入请求头
- ✅ 统一错误处理 - 拦截器 + Toast 通知

## 快速开始

```bash
pnpm install    # 安装依赖
pnpm dev        # 启动开发服务器 (http://localhost:3000)
pnpm build      # 构建静态导出 (输出到 out/)
pnpm lint       # ESLint 检查
```

## 架构设计

### 分层架构

```
frontend/
├── app/                    # 【页面层】Next.js App Router
│   ├── [locale]/           # 国际化路由参数
│   │   ├── (auth)/         # 路由组: 认证页面（不影响 URL）
│   │   │   ├── login/      # /en-US/login
│   │   │   ├── register/   # /en-US/register
│   │   │   └── forgot-password/
│   │   └── (app)/          # 路由组: 应用主体
│   │       ├── page.tsx    # /en-US/ (仪表盘)
│   │       └── layout.tsx  # 应用布局（侧边栏）
│   └── oauth/linuxdo/callback/  # OAuth 回调
│
├── components/             # 【组件层】
│   ├── ui/                 # shadcn/ui 基础组件
│   ├── login-form.tsx      # 业务组件: 登录表单
│   ├── auth-guard.tsx      # 认证保护组件
│   └── language-toggle.tsx # 语言切换器
│
├── lib/                    # 【业务逻辑层】
│   ├── api-client.ts       # axios 实例 + 拦截器
│   ├── api/auth.ts         # API 方法封装
│   ├── auth-context.tsx    # 认证上下文 (useAuth)
│   ├── i18n-client.tsx     # 国际化客户端 (useTranslation)
│   └── dictionaries.ts     # 字典加载 (服务端)
│
├── hooks/                  # 【数据层】
│   ├── use-locale.ts       # 获取当前语言
│   └── use-mobile.ts       # 检测移动设备
│
└── dictionaries/           # 翻译字典
    ├── en-US.json
    └── zh-CN.json
```

### 核心架构原理

#### 1. 静态导出架构

**配置**: `next.config.ts`
```typescript
{
  output: 'export',           // 静态导出
  trailingSlash: true,
  images: { unoptimized: true }
}
```

**关键文件**:
- `app/[locale]/layout.tsx` - 使用 `generateStaticParams()` 为每个语言生成静态页面
- `app/[locale]/layout.tsx` - 设置 `dynamicParams = false` 禁用动态参数

**限制**:
- ❌ 不支持 SSR / API Routes / 图片优化 / 动态路由
- ✅ 所有组件都是 Client Component (`'use client'`)
- ✅ 可部署到任何静态服务器 (Nginx, CDN)

#### 2. 国际化架构

**三层 I18n 系统**:

```
构建时 (Build Time):
  app/[locale]/layout.tsx (Server Component)
    ↓ getDictionary(locale) 加载 JSON
    ↓ 传递给 I18nProvider

运行时 (Client Side):
  任何组件 ('use client')
    ↓ useTranslation() 获取字典
```

**关键文件**:
- `lib/i18n-config.ts` - 语言配置 (`locales`, `defaultLocale`)
- `lib/dictionaries.ts` - 字典加载器 (服务端，`getDictionary()`)
- `lib/i18n-client.tsx` - 客户端 Context (`I18nProvider`, `useTranslation()`)
- `app/[locale]/layout.tsx` - 在构建时加载字典并传递给 Provider

**实际使用**: 参考 `app/[locale]/(auth)/login/page.tsx`
```typescript
'use client'
import { useTranslation } from '@/lib/i18n-client'

const dict = useTranslation()
<h1>{dict.auth.login.title}</h1>
```

**语言切换**: 参考 `components/language-toggle.tsx`
- 使用 `usePathname()` 解析当前语言
- 使用 `router.push()` + `router.refresh()` 切换语言

#### 3. 认证架构

**认证流程**:
```
登录 → authApi.login() → 保存 Token 到 localStorage
  ↓
login(accessToken, refreshToken) → 更新 AuthContext
  ↓
AuthGuard 检查 isAuthenticated → 渲染内容
```

**关键文件**:
- `lib/auth-context.tsx` - 认证 Context (`AuthProvider`, `useAuth()`)
  - 提供: `isAuthenticated`, `isLoading`, `user`, `login()`, `logout()`
  - Token 存储在 localStorage (`access_token`, `refresh_token`)
- `components/auth-guard.tsx` - 认证保护组件
  - 未登录时重定向到 `/[locale]/login`

**实际使用**: 参考 `app/[locale]/(app)/layout.tsx` 和 `components/login-form.tsx`

#### 4. API 客户端架构

**拦截器模式**:
```
请求拦截器 → 自动添加 Headers
  - X-Device-Id (UUID, localStorage)
  - Accept-Language (当前语言)
  - Authorization (Bearer Token)

响应拦截器 → 统一错误处理
  - 根据 errorHandler 配置显示 Toast
```

**关键文件**:
- `lib/api-client.ts` - axios 实例 + 拦截器
  - `apiClient` - 配置好的 axios 实例
  - `request<T>()` - 通用请求方法
  - `ApiResponse<T>` - 统一响应类型
- `lib/api/auth.ts` - 认证 API 封装
  - `authApi.login()`, `authApi.register()`, `authApi.logout()` 等

**实际使用**: 参考 `components/login-form.tsx`
```typescript
const response = await authApi.login(data, {
  showToast: true,      // 错误时显示 Toast
  toastType: 'error'
})
```

#### 5. 路由和导航

**路由组 (Route Groups)**:
- `(auth)/` - 认证页面组（简洁布局）
- `(app)/` - 应用主体页面组（侧边栏布局）

**URL 映射**:
- `app/[locale]/(auth)/login/page.tsx` → `/en-US/login`
- `app/[locale]/(app)/page.tsx` → `/en-US/`

**获取当前语言**: `hooks/use-locale.ts`
```typescript
const locale = useLocale()  // 'en-US' | 'zh-CN'
```

**导航时保持语言**:
```typescript
router.push(`/${locale}/settings`)
```

## 核心模块索引

| 模块 | 关键文件 | 核心功能 | 主要导出 |
|------|---------|---------|---------|
| **API 客户端** | `lib/api-client.ts` | axios 实例、拦截器 | `apiClient`, `request<T>()` |
| **认证上下文** | `lib/auth-context.tsx` | 管理登录状态 | `useAuth()`, `AuthProvider` |
| **认证保护** | `components/auth-guard.tsx` | 保护需登录页面 | `<AuthGuard>` |
| **国际化配置** | `lib/i18n-config.ts` | 语言配置 | `Locale`, `i18n` |
| **国际化客户端** | `lib/i18n-client.tsx` | 客户端字典访问 | `useTranslation()`, `I18nProvider` |
| **字典加载** | `lib/dictionaries.ts` | 服务端字典加载 | `getDictionary()` |
| **认证 API** | `lib/api/auth.ts` | 登录/注册/登出 | `authApi.*` |
| **获取语言** | `hooks/use-locale.ts` | 获取当前语言 | `useLocale()` |
| **语言切换** | `components/language-toggle.tsx` | 语言切换 UI | `<LanguageToggle>` |
| **错误处理** | `lib/error-handler.ts` | Toast 通知 | `showApiError()` |

> 💡 **学习方法**: 阅读上述文件的源码，理解实际实现。文件都有详细的 JSDoc 注释。

## 开发规范

### 文件组织

```bash
# ✅ 页面放在 app/[locale]/ 下
app/[locale]/(app)/settings/page.tsx

# ✅ 组件放在 components/
components/settings-form.tsx

# ✅ API 按业务模块组织
lib/api/auth.ts
lib/api/user.ts

# ❌ 不要在 lib/ 下放组件
lib/components/settings-form.tsx  # 错误
```

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 文件名 | kebab-case | `user-profile.tsx` |
| 组件名 | PascalCase | `UserProfile` |
| 函数名 | camelCase | `getUserProfile` |
| 常量 | UPPER_CASE | `API_BASE_URL` |
| Hook | use* | `useAuth` |

### 代码风格

**TypeScript**:
```typescript
// ✅ 必须定义 Props 类型
interface UserProfileProps {
  userId: number
}

export function UserProfile({ userId }: UserProfileProps) { }

// ❌ 不定义类型
export function UserProfile({ userId }) { }
```

**Client Component**:
```typescript
// ✅ 使用 Hooks 必须声明 'use client'
'use client'
import { useState } from 'react'

// ❌ 缺少 'use client' 会报错
import { useState } from 'react'
```

**样式**:
```typescript
// ✅ 使用 Tailwind 类名
<div className="flex items-center gap-4 p-4">

// ❌ 内联样式
<div style={{ display: 'flex', padding: '16px' }}>
```

### 禁止事项

#### ❌ 直接调用 axios

```typescript
// ❌ 错误
import axios from 'axios'
await axios.post('/api/auth/login', data)

// ✅ 正确 - 使用封装的 API 方法
import { authApi } from '@/lib/api/auth'
await authApi.login(data)
```

#### ❌ 硬编码文本

```typescript
// ❌ 错误
<h1>登录</h1>

// ✅ 正确 - 使用国际化
const dict = useTranslation()
<h1>{dict.auth.login.title}</h1>
```

#### ❌ 使用相对路径导航

```typescript
// ❌ 错误 - 会丢失语言参数
router.push('/dashboard')

// ✅ 正确 - 保持语言参数
const locale = useLocale()
router.push(`/${locale}/dashboard`)
```

#### ❌ localStorage 存储敏感信息

```typescript
// ❌ 错误 - 不要存储密码、信用卡
localStorage.setItem('password', userPassword)

// ✅ 正确 - 只存储 Token
localStorage.setItem('access_token', token)
```

## 常见开发任务

### 添加新页面

**参考文件**: `app/[locale]/(auth)/login/page.tsx` 或 `app/[locale]/(app)/page.tsx`

1. 确定页面类型（认证页面 or 应用页面）
2. 创建 `page.tsx` 文件（必须是 Client Component）
3. 使用 `useTranslation()` 获取翻译
4. 应用页面需要包裹 `<AuthGuard>`

### 调用 API

**参考文件**: `components/login-form.tsx`, `lib/api/auth.ts`

1. 在 `lib/api/*.ts` 中封装 API 方法
2. 使用 `request<T>()` 发起请求
3. 配置 `errorHandler` 控制错误处理
4. 在组件中调用 API 方法

### 处理表单

**参考文件**: `components/login-form.tsx`, `components/register-form.tsx`

1. 使用 `useState` 管理表单数据和错误
2. 使用 `useTranslation()` 获取错误消息
3. 在 `handleSubmit` 中验证和提交
4. 使用 `isLoading` 状态禁用按钮

**邮箱验证码倒计时**: 参考 `components/register-form.tsx`

### 实现国际化

**参考文件**: `dictionaries/zh-CN.json`, `app/[locale]/(auth)/login/page.tsx`

1. 在 `dictionaries/*.json` 添加翻译
2. 在组件中使用 `useTranslation()` 获取字典
3. 访问翻译: `dict.auth.login.title`

### 添加 shadcn/ui 组件

**安装组件**:
```bash
pnpm dlx shadcn@latest add dialog
pnpm dlx shadcn@latest add button
pnpm dlx shadcn@latest add input
# 支持的组件: alert, avatar, badge, card, dialog, dropdown-menu, form, input, select, sheet, skeleton, table, tabs, toast 等
```

**组件列表**: 参考 https://ui.shadcn.com/components

**完整文档**: 包含所有组件的详细使用说明，参考 `docs/llm/shadcn-llms.txt`（从 https://ui.shadcn.com/llms.txt 获取）

**实际使用**: 参考 `components/ui/` 下的组件文件

### 添加认证保护

**参考文件**: `components/auth-guard.tsx`, `app/[locale]/(app)/layout.tsx`

使用 `<AuthGuard>` 包裹需要保护的内容:
```typescript
<AuthGuard>
  <ProtectedContent />
</AuthGuard>
```

### 数据获取 (SWR)

**参考文件**: `app/[locale]/(app)/page.tsx`

```typescript
import useSWR from 'swr'
import { getApiUrl, fetcher } from '@/lib/api-client'

const { data, error, isLoading } = useSWR(
  getApiUrl('/api/v1/endpoint'),
  fetcher
)
```

## 调试指南

### 检查认证状态

```typescript
// 在组件中
const { isAuthenticated, user } = useAuth()
console.log('isAuthenticated:', isAuthenticated)
console.log('user:', user)

// 在浏览器 Console
console.log(localStorage.getItem('access_token'))
console.log(localStorage.getItem('refresh_token'))
```

### 检查网络请求

**浏览器 DevTools → Network**:
1. 检查请求 Headers:
   - `X-Device-Id` 是否存在
   - `Accept-Language` 是否正确 (zh-CN / en-US)
   - `Authorization` 是否包含 Bearer Token
2. 检查响应:
   - Status Code: 200/401/500
   - Response Body: `{ code, message, data, traceId }`

### 检查国际化

```typescript
// 检查当前语言
const locale = useLocale()
console.log('locale:', locale)

// 检查 URL
console.log('pathname:', window.location.pathname)  // 应包含 /en-US/ 或 /zh-CN/
```

## 注意事项

### 静态导出限制

| 功能 | 是否支持 |
|------|---------|
| SSR (服务端渲染) | ❌ |
| API Routes | ❌ |
| Next.js Image Optimization | ❌ |
| 动态路由 | ❌ |
| 客户端导航 | ✅ |
| 静态文件服务 | ✅ |

### 环境变量

客户端访问的变量**必须**以 `NEXT_PUBLIC_` 开头:

```bash
# ✅ 正确
NEXT_PUBLIC_API_URL=http://localhost:8080

# ❌ 错误 - 客户端访问不到
API_URL=http://localhost:8080
```

### Token 存储

当前使用 localStorage 存储 Token（方便但有 XSS 风险）。生产环境建议:
- 使用 httpOnly Cookie
- 实现 Token 自动刷新（参考 `lib/api-client.ts` 响应拦截器）
- 设置合理的过期时间

## 关键设计决策

### 为什么使用静态导出？

- ✅ 部署简单（无需 Node.js）
- ✅ 性能极佳（预生成）
- ✅ 安全性高（无后端）
- ❌ 不支持动态内容

### 为什么分离认证和应用页面？

使用路由组 `(auth)` 和 `(app)` 实现不同的布局:
- 认证页面: 简洁居中布局
- 应用页面: 侧边栏 + 导航栏布局

### 为什么使用 SWR？

- 轻量 (~13KB)
- 自动缓存
- 失焦刷新
- 简单 API

## 性能优化

1. **代码分割**: 使用 `dynamic import`
   ```typescript
   const Chart = dynamic(() => import('@/components/chart'), {
     loading: () => <Skeleton />
   })
   ```

2. **SWR 缓存**: 自动缓存和去重请求

3. **延迟加载图片**:
   ```typescript
   <img src="/avatar.jpg" loading="lazy" />
   ```

## 部署

```bash
# 构建
pnpm build  # 生成 out/ 目录

# 部署
# 将 out/ 目录上传到静态服务器 (Nginx, S3, GitHub Pages)
```

**环境变量**: 在 `.env.local` 设置 `NEXT_PUBLIC_API_URL`
