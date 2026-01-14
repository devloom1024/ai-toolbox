# /piv:execute - 执行实施计划

按照 PLAN 阶段制定的计划进行代码实现。

## 输入

`.agents/plans/{日期}-{任务名}.md` 中的实施计划

## 执行步骤

### 1. 准备工作
- 确认计划文件存在且是最新的
- 检查是否有未解决的技术决策
- 确认开发环境已就绪

### 2. 阶段执行

> **提示**: 开始编码前，建议先阅读对应技术栈的规范文档，确保代码符合项目标准。

#### 后端开发 (backend)
```bash
cd backend
./mvnw spring-boot:run    # 启动开发服务器
```

**参考规范**:
- [spring-boot-best-practices.md](../reference/spring-boot-best-practices.md) - DDD 架构、事务、异常处理
- [postgresql-best-practices.md](../reference/postgresql-best-practices.md) - 数据库设计、Flyway 迁移
- [api-best-practices.md](../reference/api-best-practices.md) - REST API 设计

**遵循 DDD 分层**:
- `{domain}/api/` - Controller
- `{domain}/service/` - 业务逻辑
- `{domain}/domain/` - 实体/枚举
- `{domain}/dto/` - Request/Response

#### 前端开发 (frontend)
```bash
cd frontend
pnpm dev                  # 启动开发服务器
```

**参考规范**:
- [nextjs-best-practices.md](../reference/nextjs-best-practices.md) - 静态导出、国际化、组件开发
- [docs/llm/shadcn-llms.txt](../docs/llm/shadcn-llms.txt) - shadcn/ui 组件使用指南

**遵循分层**:
- `app/[locale]/` - 页面
- `components/` - 组件
- `lib/` - 业务逻辑
- `hooks/` - 数据获取

#### Python 网关开发 (python-services)
```bash
cd python-services
uv run python run.py      # 启动开发服务器
```

**参考规范**:
- [fastapi-best-practices.md](../reference/fastapi-best-practices.md) - FastAPI 架构、模块化设计

**遵循模块化架构**:
- `gateway/modules/` - 数据源模块
- `gateway/routers/` - API 路由

### 3. 代码规范检查

#### 后端
> 参考: [spring-boot-best-practices.md](../reference/spring-boot-best-practices.md)
- ✅ 使用 `ApiResponse<T>` 统一响应
- ✅ 从 `DeviceContextHolder` 获取设备 ID
- ✅ 从 `CurrentUser` 获取当前用户
- ✅ 事务使用 `@Transactional(rollbackFor = {Exception.class, Error.class})`
- ✅ 业务异常使用 `BizException(BizErrorCode)`

#### 前端
> 参考: [nextjs-best-practices.md](../reference/nextjs-best-practices.md)
- ✅ 使用 `useTranslation()` 国际化
- ✅ 页面放在 `app/[locale]/` 下
- ✅ API 调用封装在 `lib/api/*.ts`
- ✅ 路由保持语言前缀

#### Python
> 参考: [fastapi-best-practices.md](../reference/fastapi-best-practices.md)
- ✅ 使用 `QuoteResponse` 统一响应
- ✅ 遵循 `gateway/modules/` 模块化结构
- ✅ ruff 检查通过

### 4. 进度更新
- 在计划文件中标记完成的任务
- 记录遇到的问题和解决方案

## 输出

- ✅ 完成代码实现
- ✅ 更新 `.agents/plans/{任务名}.md` 进度
- ✅ 记录任何偏差和未解决问题