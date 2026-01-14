# 实施计划：投资模块

## 概述

**目标**: 在项目中添加完整的投资模块，支持 A股、港股、美股、ETF 和基金的搜索、自选管理和详情查看。

**核心功能**:
1. 标的搜索（按名称/代码，支持多市场）
2. 自选管理（添加/移除/列表）
3. 标的详情（参考同花顺展示实时行情、K线、基本面数据）

## 范围

### 包含
- Python 网关：扩展 akshare 模块，添加 ETF/基金搜索接口
- 后端：重建 investment 模块（DDD 分层）
- 前端：搜索页、自选列表页、详情页
- 数据库：投资账号、自选表、标的缓存表

### 不包含
- 交易功能（买卖委托）
- 组合管理（持仓盈亏计算）
- 行情推送（WebSocket）
- 智能投顾/推荐

## 任务列表

## 模块目录结构

采用按功能细分 + DDD 分层的混合架构：

```
backend/src/main/java/com/devloom/ai/toolbox/investment/
├── search/              # 搜索功能
│   ├── api/             # SearchController
│   ├── domain/          # 实体、仓库
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   └── service/
├── watchlist/           # 自选功能
│   ├── api/             # WatchlistController
│   ├── domain/          # 实体、仓库、枚举、转换器
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── enums/
│   │   └── converter/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── service/
│   │   └── support/     # 内部辅助类
│   └── infra/           # 基础设施
├── security/            # 标的详情功能
│   ├── api/             # SecurityController
│   ├── domain/
│   ├── dto/
│   └── service/
└── common/              # 投资模块公共组件（如异常定义）
```

**说明**：
- API 层放各功能目录顶层，便于路由统一管理
- 公共组件（如自定义异常）放 `common`
- 遵循 DDD 分层：`api` → `service` → `domain`

## 任务列表

### 阶段一：后端基础设施

| 任务 | 文件 | 描述 | 验收标准 | 状态 |
|------|------|------|---------|------|
| 1.1 | `backend/src/main/resources/db/migration/V2__init_investment_schema.sql` | 创建投资模块数据库表 | Flyway 迁移成功 | ✅ |
| 1.2 | `backend/src/main/java/com/devloom/ai/toolbox/investment/watchlist/domain/entity/` | 创建 WatchlistEntity | Entity 与 DDL 对应 | ✅ |
| 1.3 | `backend/src/main/java/com/devloom/ai/toolbox/investment/watchlist/domain/repository/` | 创建 WatchlistRepository | JPA 方法可用 | ✅ |
| 1.4 | `backend/src/main/java/com/devloom/ai/toolbox/investment/search/dto/` | 创建 SearchDTO | 符合命名规范 | ✅ |
| 1.5 | `backend/src/main/java/com/devloom/ai/toolbox/investment/watchlist/service/` | 创建 WatchlistService | 单元测试通过 | ✅ |
| 1.6 | `backend/src/main/java/com/devloom/ai/toolbox/investment/security/service/` | 创建 SecurityService | 单元测试通过 | ✅ |

### 阶段二：后端 API

| 任务 | 文件 | 描述 | 验收标准 | 状态 |
|------|------|------|---------|------|
| 2.1 | `backend/src/main/java/com/devloom/ai/toolbox/investment/search/api/SearchController.java` | 标的搜索接口 | 返回搜索结果 | ✅ |
| 2.2 | `backend/src/main/java/com/devloom/ai/toolbox/investment/watchlist/api/WatchlistController.java` | 自选管理接口（CRUD） | 正常添加/移除 | ✅ |
| 2.3 | `backend/src/main/java/com/devloom/ai/toolbox/investment/security/api/SecurityController.java` | 标的详情接口 | 返回完整详情 | ✅ |
| 2.4 | `backend/src/main/java/com/devloom/ai/toolbox/common/security/SecurityConfig.java` | 更新安全配置 | 接口可访问 | ✅ |

### 阶段三：Python 网关扩展

| 任务 | 文件 | 描述 | 验收标准 |
|------|------|------|---------|
| 3.1 | `python-services/gateway/modules/akshare/client.py` | 添加 ETF/基金搜索方法 | 返回基金列表 |
| 3.2 | `python-services/gateway/modules/akshare/mapper.py` | 统一字段映射（中文→英文） | 数据格式一致 |
| 3.3 | `python-services/gateway/routers/akshare.py` | 添加基金搜索端点 | API 可调用 |
| 3.4 | `python-services/gateway/routers/security.py` | 新建标的详情路由 | 返回完整数据 |
| 3.5 | `python-services/gateway/routers/security.py` | 添加分组管理端点 | 支持 CRUD |

### 阶段四：前端页面

| 任务 | 文件 | 描述 | 验收标准 |
|------|------|------|---------|
| 4.1 | `frontend/app/[locale]/(app)/investment/search/page.tsx` | 搜索页面 | 可按名称/代码搜索，添加自选 |
| 4.2 | `frontend/app/[locale]/(app)/investment/watchlist/page.tsx` | 自选列表页 | 显示持仓信息，含搜索入口 |
| 4.3 | `frontend/app/[locale]/(app)/investment/detail/[symbol]/page.tsx` | 标的详情页 | 显示 K线/基本面 |
| 4.4 | `frontend/components/investment/` | 通用组件（搜索框、股票卡片、K线图） | 组件可复用 |

### 阶段五：详情页数据展示（按市场）

| 数据类型 | A_SHARE | HK | US | ETF | FUND | 数据源 |
|---------|---------|-----|-----|-----|------|--------|
| 实时行情 | ✓ | ✓ | ✓ | ✓ | ✓ | akshare realtime |
| K线图 | ✓ | ✓ | ✓ | ✓ | ✓ | akshare kline |
| 基本面 | ✓ | - | - | - | - | akshare fundamental |
| 财务指标 | ✓ | - | - | - | - | akshare financial |
| 资金流向 | ✓ | - | - | - | - | akshare capital |
| 净值数据 | - | - | - | ✓ | ✓ | akshare fund |
| 持仓数据 | - | - | - | - | ✓ | akshare fund |
| 分红数据 | - | - | - | ✓ | - | akshare fund |

K线周期：D（日K）、W（周K）、M（月K）、1/5/15/30/60（分钟K）

**注意**：1 分钟数据仅返回近 5 个交易日且不复权

## 响应结构规范

### 统一 API 响应格式
```json
{
  "code": 0,
  "message": "ok",
  "data": { ... },
  "traceId": "string"
}
```

### 请求参数规范
- **搜索**: `keyword` + `market`(可选) + `limit` + `offset`
- **自选分页**: `page`(从1开始) + `size`
- **标的代码**: 纯数字/字母（如 `600519`）
- **市场类型**: `A_SHARE`, `HK`, `US`, `ETF`, `FUND`, `ALL`

## 技术决策

### 决策点 1：K线图前端渲染方案

**选项**:
- A. ECharts - 功能丰富，支持多种图表，与后端 shadcn/ui 风格一致
- B. TradingView Lightweight Charts - 专为金融图表设计
- C. Recharts - React 生态，简洁但功能有限

**决定**: A（ECharts）
- 原因：与后端 UI 风格一致，生态丰富，社区活跃

### 决策点 2：标的代码格式

**选项**:
- A. 复合编码 `market:symbol`（如 `SH:600519`）
- B. 前缀标识 `SH600519`、`HK00700`、`US:AAPL`
- C. 统一市场字段 + 独立代码（纯数字/字母）

**决定**: C
- 原因：与 OpenAPI 规范一致，简洁清晰

### 决策点 3：数据缓存策略

**选项**:
- A. 后端 Redis 缓存（实时数据除外）
- B. Python 网关层缓存
- C. 前端 SWR 缓存

**推荐**: A + C
- 原因：后端缓存热门标的减少 API 调用，前端 SWR 提升用户体验

## 风险

| 风险 | 描述 | 缓解措施 |
|-----|------|---------|
| akshare 接口稳定性 | 第三方数据源可能变更或限流 | 添加错误处理、降级逻辑 |
| 前端 K线图性能 | 大量数据渲染可能卡顿 | 分页加载、使用轻量图表库 |
| 后端跨域 | 前后端分离可能存在跨域 | 配置 CORS 白名单 |
| 数据库并发 | 自选操作可能并发冲突 | 添加乐观锁 |

## 依赖关系

```
阶段一 → 阶段二 → 阶段三 → 阶段四 → 阶段五
   ↓          ↓         ↓
后端 DB    后端 API   Python 扩展
```

## 外部依赖

- **akshare 库**: 金融数据获取
- **ECharts**: K线渲染
- **shadcn/ui**: 前端 UI 组件
