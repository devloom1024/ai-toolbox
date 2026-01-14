# PIV Loop AI 工作流

本项目使用 PIV Loop (Prime-Plan-Execute-Validate) AI 开发工作流。

## 工作流程

```
PRIME → PLAN → EXECUTE → VALIDATE
```

### 1. PRIME 阶段 (/piv:prime)

理解项目结构、技术栈和开发规范。

**输出**: 项目概览、技术栈确认、待确认事项

### 2. PLAN 阶段 (/piv:plan)

根据需求创建详细的实施计划。

**输出**: `.agents/plans/{日期}-{任务名}.md`

### 3. EXECUTE 阶段 (/piv:execute)

按计划进行代码实现。

**输出**: 代码变更、更新计划进度

### 4. VALIDATE 阶段 (/piv:validate)

运行 lint、test 和 build 验证代码质量。

**输出**: 验证结果报告

## 快速命令

| 命令 | 描述 |
|------|------|
| `/piv:prime` | 理解项目 |
| `/piv:plan` | 创建计划 |
| `/piv:execute` | 执行实现 |
| `/piv:validate` | 验证质量 |
| `/piv:commit` | 智能提交 |

## 项目结构

```
.agents/
├── plans/              # 实施计划
│   └── {日期}-{任务名}.md
├── code-reviews/       # 代码审查
│   └── {日期}-{PR号}.md
└── README.md           # 本文件
```

## 技术栈

| 子项目 | 框架 | 包管理器 |
|--------|------|---------|
| backend | Spring Boot 4.0.1 + Java 21 | Maven |
| frontend | Next.js 16 + React 19 + TypeScript | pnpm |
| python-services | FastAPI + Python 3.11 | uv |

## 参考文档

**技术栈最佳实践**:
- 后端规范: `.claude/reference/spring-boot-best-practices.md`
- 前端规范: `.claude/reference/nextjs-best-practices.md`
- Python 规范: `.claude/reference/fastapi-best-practices.md`

**数据库与 API 规范**:
- PostgreSQL 规范: `.claude/reference/postgresql-best-practices.md`
- Redis 规范: `.claude/reference/redis-best-practices.md`
- API 设计规范: `.claude/reference/api-best-practices.md`
- OpenAPI 规范: `.claude/reference/openapi-best-practices.md`
