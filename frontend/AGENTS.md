# Frontend 项目规范

Next.js 16 + TypeScript + Tailwind CSS + shadcn/ui 前端项目。

## 技术栈

- **框架**: Next.js 16 (App Router)
- **语言**: TypeScript 5
- **样式**: Tailwind CSS 4 + shadcn/ui + CSS Variables
- **组件库**: Radix UI primitives + Lucide icons
- **状态管理**: SWR + React Context
- **HTTP**: Axios
- **包管理**: pnpm 10.19.0

## 命令

```bash
pnpm dev              # 开发服务器 (http://localhost:3000)
pnpm build            # 生产构建
pnpm start            # 启动生产服务器
pnpm lint             # ESLint 检查 (使用 eslint.config.mjs)
```

## 目录结构

```
frontend/
├── app/              # Next.js App Router 页面 (含 i18n [locale])
├── components/       # 组件
│   ├── ui/          # shadcn/ui 基础组件
│   └── ...
├── lib/              # 工具函数和配置
│   ├── api/         # API 客户端
│   ├── utils.ts     # cn() 工具函数
│   └── i18n-config.ts
├── hooks/            # 自定义 React Hooks
├── dictionaries/     # i18n 字典 (zh-CN.json, en-US.json)
└── public/           # 静态资源
```

## 代码规范

### 命名规范

- **组件**: PascalCase (`UserProfile.tsx`)
- **文件**: camelCase (`authApi.ts`, `useAuth.ts`)
- **CSS 类**: kebab-case 或 Tailwind 类名

### 导入顺序

```typescript
// 1. React 相关
import { useState, useEffect } from "react"

// 2. Next.js 相关
import Link from "next/link"
import { useRouter } from "next/navigation"

// 3. 第三方库
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import { useQuery } from "swr"

// 4. 内部模块
import { authApi } from "@/lib/api/auth"
import { useAuth } from "@/hooks/useAuth"
```

### 组件规范

- 使用函数式组件 + TypeScript 类型
- 使用 shadcn/ui 组件风格 (cva 管理变体)
- UI 组件放在 `@/components/ui/`
- 业务组件放在 `@/components/` 对应目录
- Props 类型使用 TypeScript interface

```tsx
// 正确示例
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "default" | "destructive" | "outline"
}

export function Button({ className, variant = "default", ...props }: ButtonProps) {
  return <button className={cn(baseStyles, variants[variant], className)} {...props} />
}
```

### API 客户端

- 使用 Axios 封装 API
- 响应格式统一使用 `ApiResponse<T>`
- 错误处理在 API 层统一处理

### i18n

- 文本放在 `dictionaries/` 目录
- 使用 `useTranslation` 或直接读取字典
- 键名使用描述性名称: `auth.login.success`

### 样式

- 使用 Tailwind CSS 类名
- 使用 CSS Variables 实现暗色模式
- shadcn/ui 风格: `new-york`, zinc 基础色

### 其他

- 禁用 `any` 类型
- 禁用 console.log (调试用除外)
- 组件文件不超过 300 行，必要时拆分
- API 错误提示使用 Sonner Toast
