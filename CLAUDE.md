# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个全栈 AI 工具箱项目,采用前后端分离架构:
- **后端**: Spring Boot 4.0.1 + Java 21 + PostgreSQL + Redis
- **前端**: Next.js 16 (静态导出模式) + React 19 + TypeScript + Tailwind CSS

## 常用命令

### 后端 (backend/)

```bash
# 启动开发环境 (含 Docker Compose PostgreSQL/Redis)
./mvnw spring-boot:run

# 运行测试
./mvnw test

# 单个测试
./mvnw test -Dtest=ClassName#methodName

# 编译打包
./mvnw clean package

# 启动 Docker Compose 服务
docker compose up -d

# 停止 Docker Compose 服务
docker compose down
```

### 前端 (frontend/)

```bash
# 安装依赖 (使用 pnpm)
pnpm install

# 启动开发服务器
pnpm dev

# 构建静态导出
pnpm build

# Lint 检查
pnpm lint
```

## 架构设计

### 后端架构

采用**领域驱动设计 (DDD)** 分层架构,包结构为:

```
com/devloom/ai/toolbox/
├── {domain}/                  # 业务领域模块 (如 auth)
│   ├── api/                   # Controller 层 - 处理 HTTP 请求
│   ├── service/               # Application Service 层 - 业务编排
│   ├── domain/                # 领域层
│   │   ├── entity/            # JPA 实体
│   │   ├── repository/        # Repository 接口
│   │   ├── enums/             # 领域枚举
│   │   └── converter/         # 类型转换器
│   ├── dto/                   # 数据传输对象
│   │   ├── request/           # 请求 DTO
│   │   └── response/          # 响应 DTO
│   └── infra/                 # 基础设施层 (如邮件客户端)
└── common/                    # 跨领域公共组件
    ├── config/                # 全局配置
    ├── security/              # 安全框架 (JWT/Spring Security)
    ├── web/                   # Web 过滤器和上下文
    ├── exception/             # 异常处理和错误码
    └── response/              # 统一响应封装 (ApiResponse)
```

#### 关键设计模式

1. **统一响应封装**: 所有 Controller 返回 `ApiResponse<T>`,禁止直接返回 `ResponseEntity` 或裸 DTO
2. **设备上下文管理**: `DeviceContextFilter` 拦截 `X-Device-Id` 头并注入 `DeviceContextHolder`,Controller 不直接读取请求头
3. **国际化支持**:
   - 所有用户可见的消息必须从 `messages/*.properties` 读取
   - Bean Validation 的 `message` 属性必须引用国际化资源键
4. **JWT 认证**: `JwtAuthenticationFilter` 处理 Bearer Token 验证,用户信息存储在 `CurrentUser` (ThreadLocal)
5. **OAuth 集成**: LinuxDo OAuth 流程采用前端主导重定向,后端返回 `LinuxDoAuthorizeResponse` (含 `state` 和 `authorizeUrl`)

### 前端架构

采用 **Next.js App Router** + 静态导出模式:

```
frontend/
├── app/                       # Next.js App Router
│   ├── [locale]/              # 国际化路由
│   │   ├── (auth)/            # 认证相关页面组 (login, register)
│   │   └── (app)/             # 应用主体页面组
│   └── layout.tsx             # 全局布局
├── components/                # React 组件
├── hooks/                     # 自定义 Hooks
├── lib/                       # 工具库
└── dictionaries/              # 国际化字典
```

#### 关键配置

- **静态导出**: `output: 'export'` - 无需 Node.js 运行时,生成纯静态文件到 `out/`
- **API 调用**: 使用环境变量 `NEXT_PUBLIC_API_URL` 配置后端地址
- **状态管理**: 使用 SWR 进行数据获取和缓存

### 数据库迁移

使用 **Flyway** 管理数据库版本:
- 迁移脚本位于 `backend/src/main/resources/db/migration/`
- 命名规范: `V{版本号}__{描述}.sql` (如 `V1__init_auth_schema.sql`)
- PostgreSQL 特有类型 (INET/JSONB/SMALLINT) 需在 Entity 中添加 `@JdbcTypeCode` 或 `AttributeConverter`

### 环境变量管理

- **后端**: 所有环境变量统一在 `backend/.env` 中定义,Spring Boot 和 Docker Compose 共用
- **前端**: 使用 `frontend/.env.local` (或生产环境的对应文件)

## 后端开发规范 (详见 backend/AGENTS.md)

**必须遵守的规则**:

1. **Controller 层**:
   - 必须返回 `ApiResponse<T>`
   - 不得使用 `@RequestHeader` 读取设备号,应从 `DeviceContextHolder` 获取
   - 每个方法必须有注释,所有 DTO 字段必须有注释

2. **DTO 命名规范** (详见 `docs/specs/openapi.md`):
   - 请求 DTO 必须以 `Request` 结尾
   - 响应 DTO 必须以 `Response` 结尾
   - 与 OpenAPI Schema 保持严格一致

3. **Entity 层**:
   - 所有字段必须有注释,与 `db/migration` 中的 DDL 保持一致
   - PostgreSQL 特有类型必须使用 `@JdbcTypeCode`/`columnDefinition` 或自定义转换器

4. **Service 层**:
   - `@Transactional` 必须显式声明 `rollbackFor = {Exception.class, Error.class}`
   - 严禁通过 `this` 调用同一 Bean 内的其他 `@Transactional` 方法 (会导致事务代理失效)

5. **国际化**:
   - 所有枚举必须有注释说明语义
   - Bean Validation 的 `message` 不得为空,必须引用国际化键
   - 所有返回给前端的消息必须支持国际化

## API 规范

参考 `docs/specs/openapi.md`:
- 统一使用 `ApiResponse` 作为响应包装
- 错误响应以 `ApiResponseError` 命名
- 成功响应建议以 `ApiResponse{业务概念}` 命名

## 测试策略

后端使用 **Testcontainers** 进行集成测试,自动启动 PostgreSQL/Redis 容器。

## 注意事项

- **事务陷阱**: Spring AOP 代理机制要求跨方法调用才能触发事务,内部调用需注入自身代理或抽取独立组件
- **Flyway 校验**: 修改 Entity 后务必对照迁移脚本进行 schema 校验再提交
- **静态导出限制**: 前端禁用图片优化 (`images.unoptimized: true`),不支持服务端渲染
