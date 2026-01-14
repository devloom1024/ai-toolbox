# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个全栈 AI 工具箱项目,采用前后端分离的 monorepo 架构:
- **后端**: Spring Boot 4.0.1 + Java 21 + PostgreSQL + Redis (DDD 分层架构)
- **前端**: Next.js 16 + React 19 + TypeScript (静态导出模式)
- **Python 网关**: FastAPI + akshare + Redis (金融数据网关)

## 快速开始

### 后端开发

```bash
cd backend
./mvnw spring-boot:run    # 启动开发服务器 (自动启动 Docker Compose)
./mvnw test               # 运行测试
```

### Python 网关开发

```bash
cd python-services
uv venv && source .venv/bin/activate
uv pip install -e ".[akshare,all]"  # 安装依赖
python run.py              # 启动开发服务器 (端口 8081)
```

### 前端开发

```bash
cd frontend
pnpm install              # 安装依赖
pnpm dev                  # 启动开发服务器
pnpm build                # 构建静态导出
```

## 文档资源

### 子项目文档

**详细的开发规范和架构说明请查看各子项目的 CLAUDE.md**:
- **后端**: [backend/CLAUDE.md](./backend/CLAUDE.md) - Spring Boot DDD 架构、开发规范、数据库迁移
- **前端**: [frontend/CLAUDE.md](./frontend/CLAUDE.md) - Next.js 架构、API 调用、国际化、组件开发
- **Python 网关**: [python-services/CLAUDE.md](./python-services/CLAUDE.md) - FastAPI 网关、模块化架构、数据源集成

### 项目文档 (.claude/reference/)

**技术规范和最佳实践**:
- **[.claude/reference/](./.claude/reference/)** - AI 开发参考规范
  - [openapi-best-practices.md](./.claude/reference/openapi-best-practices.md) - OpenAPI Schema 命名规范 (DTO 命名约定)
  - [api-best-practices.md](./.claude/reference/api-best-practices.md) - API 接口规范
  - [postgresql-best-practices.md](./.claude/reference/postgresql-best-practices.md) - PostgreSQL 数据库规范
  - [redis-best-practices.md](./.claude/reference/redis-best-practices.md) - Redis 缓存规范
- **[docs/design/](./docs/design/)** - 设计文档
  - [auth/](./docs/design/auth/) - 认证模块设计 (OAuth 流程、数据库设计等)

## 跨项目关键约定

### 统一响应格式

前后端使用统一的 API 响应格式 `ApiResponse<T>`:

```typescript
{
  code: number        // 业务状态码
  message: string     // 提示消息
  data: T | null      // 响应数据
  traceId: string     // 分布式追踪 ID
}
```

### 国际化

- **后端**: 所有用户消息从 `messages/*.properties` 读取 (支持 `en-US`, `zh-CN`)
- **前端**: 所有文本从 `dictionaries/*.json` 读取,路由包含语言前缀 `/{locale}/path`

### 设备标识

- **前端**: 自动生成 UUID 存储在 localStorage,每次请求通过 `X-Device-Id` header 发送
- **后端**: `DeviceContextFilter` 拦截并注入 `DeviceContextHolder`,业务代码通过 ThreadLocal 获取

### 认证机制

- **JWT**: 前端在 localStorage 存储 `access_token` 和 `refresh_token`
- **请求拦截**: 前端 axios 拦截器自动添加 `Authorization: Bearer <token>` header
- **用户上下文**: 后端 `JwtAuthenticationFilter` 验证 Token 并注入 `CurrentUser` (ThreadLocal)

### OAuth 流程

LinuxDo OAuth 采用**前端主导重定向**模式:
1. 后端返回 `{ state, authorizeUrl }`,不发起 302 重定向
2. 前端跳转到 `authorizeUrl`
3. 用户授权后回调到前端
4. 前端携带 `code` 和 `state` 调用后端完成认证

## 环境变量

- **后端**: 所有环境变量统一在 `backend/.env` 中定义,Spring Boot 和 Docker Compose 共用
- **前端**: 使用 `frontend/.env.local`,客户端访问的变量必须以 `NEXT_PUBLIC_` 开头

## 数据库迁移

使用 **Flyway** 管理数据库版本:
- 迁移脚本位于 `backend/src/main/resources/db/migration/`
- 命名规范: `V{版本号}__{描述}.sql` (如 `V1__init_auth_schema.sql`)
- 禁止修改已应用的迁移脚本,新变更需创建新版本

## API 规范

参考 `.claude/reference/openapi-best-practices.md`:
- DTO 命名: 请求 `*Request`, 响应 `*Response`
- 所有接口返回 `ApiResponse<T>` 包装
- 与 OpenAPI Schema 保持严格一致

## 测试策略

- **后端**: 使用 Testcontainers 进行集成测试,自动启动 PostgreSQL/Redis 容器
- **前端**: 使用 ESLint 进行代码检查 (`pnpm lint`)

## 重要提醒

- **事务陷阱**: Spring `@Transactional` 通过 AOP 代理实现,同一 Bean 内通过 `this` 调用会导致事务失效
- **Flyway 校验**: 修改 Entity 后务必对照迁移脚本进行 schema 校验
- **静态导出限制**: 前端不支持 SSR、API Routes、图片优化
