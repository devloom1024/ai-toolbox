# /piv:prime - 项目理解

理解当前项目结构和技术栈，为后续开发做好准备。

> **参考规范**:
> - [spring-boot-best-practices.md](../reference/spring-boot-best-practices.md)
> - [nextjs-best-practices.md](../reference/nextjs-best-practices.md)
> - [fastapi-best-practices.md](../reference/fastapi-best-practices.md)
> - [postgresql-best-practices.md](../reference/postgresql-best-practices.md)

## 执行步骤

### 1. 项目概览
- 阅读项目根目录 `CLAUDE.md` 了解整体架构
- 阅读各子项目文档：`backend/CLAUDE.md`、`frontend/CLAUDE.md`、`python-services/CLAUDE.md`

### 2. 技术栈确认
- **后端**: Spring Boot 4.0.1 + Java 21 + DDD 分层架构
- **前端**: Next.js 16 + React 19 + TypeScript + Tailwind CSS 4
- **Python 网关**: FastAPI + Python 3.11 + akshare

### 3. 跨项目关键约定

#### 统一响应格式
```typescript
{
  code: number,      // 业务状态码
  message: string,   // 提示消息
  data: T | null,    // 响应数据
  traceId: string    // 分布式追踪 ID
}
```

#### 认证机制
- JWT 存储在 localStorage（前端）
- 统一通过 `X-Device-Id` Header 标识设备
- 后端通过 `CurrentUser` (ThreadLocal) 获取当前用户

#### 国际化
- 后端: `messages/*.properties`
- 前端: `dictionaries/*.json`

### 4. 文档位置
- **API 规范**: `docs/specs/openapi.md`
- **数据库规范**: `docs/specs/postgresql.md`
- **认证设计**: `docs/design/auth/`

## 输出格式

```markdown
## 项目概览

**项目类型**: 全栈 AI 工具箱 (Monorepo)
**活跃子项目**: backend, frontend, python-services

## 技术栈

| 子项目 | 框架 | 包管理器 | 测试框架 |
|--------|------|---------|---------|
| backend | Spring Boot 4.0.1 + Java 21 | Maven | Testcontainers |
| frontend | Next.js 16 + React 19 + TypeScript | pnpm 10.19 | ESLint |
| python-services | FastAPI + Python 3.11 | uv | pytest |

## 关键架构特征

- **DDD 分层架构**: controller/service/repository
- **静态导出前端**: output: 'export'
- **Python 数据网关**: 模块化插件架构

## 待确认事项

[列出需要与用户确认的问题]
```