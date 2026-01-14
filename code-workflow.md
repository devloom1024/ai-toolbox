# AI 代码开发工作流指南

本文档指导团队如何使用 PIV Loop AI 工作流进行高效开发。

## 什么是 PIV Loop？

PIV Loop 是一个结构化的 AI 开发工作流，帮助 AI 代理系统地完成开发任务：

| 阶段 | 命令 | 描述 | 输出 |
|------|------|------|------|
| **Prime** | `/piv:prime` | 理解项目结构和规范 | 项目概览、技术栈确认 |
| **Plan** | `/piv:plan` | 制定详细实施计划 | `.agents/plans/{任务}.md` |
| **Execute** | `/piv:execute` | 按计划实现代码 | 代码变更 |
| **Validate** | `/piv:validate` | 验证代码质量 | 验证报告 |

## 快速开始

### 场景 1：开发新功能

```bash
# 1. 理解项目
/piv:prime

# 2. 创建实施计划
/piv:plan
# 输入: "添加用户设置功能，包括修改昵称、头像、密码"

# 3. 执行开发
/piv:execute

# 4. 验证质量
/piv:validate

# 5. 提交代码
/piv:commit
# 输入: "添加用户设置功能"
```

### 场景 2：修复 Bug

```bash
# 1. 理解项目（如果需要）
/piv:prime

# 2. 创建修复计划
/piv:plan
# 输入: "修复登录 Token 刷新失败的问题"

# 3. 执行修复
/piv:execute

# 4. 验证
/piv:validate

# 5. 提交
/piv:commit
# 输入: "fix(auth): 修复 Token 刷新失败问题"
```

### 场景 3：重构代码

```bash
/piv:plan
# 输入: "重构投资模块，将 Repository 层迁移到 DDD 架构"

/piv:execute

/piv:validate

/piv:commit
# 输入: "refactor(investment): 重构 Repository 层为 DDD 架构"
```

## 规范文档位置

所有开发规范集中在 `.claude/reference/` 目录：

```
.claude/reference/
├── openapi-best-practices.md      # API 命名规范
├── api-best-practices.md          # API 设计规范
├── postgresql-best-practices.md   # 数据库设计规范
├── redis-best-practices.md        # 缓存使用规范
├── spring-boot-best-practices.md  # 后端开发规范
├── nextjs-best-practices.md       # 前端开发规范
└── fastapi-best-practices.md      # Python 网关规范
```

## 工作流详细说明

### Phase 1: Prime（理解）

运行 `/piv:prime` 后，AI 会：
1. 阅读项目根目录 `CLAUDE.md` 了解整体架构
2. 阅读子项目文档（backend/CLAUDE.md 等）
3. 阅读对应的技术栈规范

**你应该**：确认 AI 理解正确，补充说明特殊需求

### Phase 2: Plan（规划）

运行 `/piv:plan` 后，AI 会：
1. 分析需求范围
2. 识别涉及的子项目和文件
3. 拆分为多个可执行的子任务
4. 生成实施计划保存到 `.agents/plans/{日期}-{任务名}.md`

**你应该**：审核计划，确认范围和优先级

### Phase 3: Execute（执行）

运行 `/piv:execute` 后，AI 会：
1. 按计划逐步实现代码
2. 遵循对应的 `*-best-practices.md` 规范
3. 更新计划进度

**你应该**：
- 定期检查代码变更
- 及时反馈需要调整的地方

### Phase 4: Validate（验证）

运行 `/piv:validate` 后，AI 会：
1. 运行各子项目的 lint 检查
2. 运行测试
3. 执行构建

**验证命令**：
```bash
# 后端
cd backend && ./mvnw test && ./mvnw spotless:check

# 前端
cd frontend && pnpm lint && pnpm build

# Python
cd python-services && uv run ruff check && uv run pytest
```

**你应该**：确保所有验证通过后再提交

### Phase 5: Commit（提交）

运行 `/piv:commit` 后，AI 会：
1. 分析所有代码变更
2. 生成符合 Angular 规范的提交信息
3. 提示你执行 `git commit`

**你应该**：确认提交信息准确，执行提交

## 项目结构参考

```
ai-toolbox/
├── .claude/                    # AI 开发配置
│   ├── commands/               # PIV Loop 命令模板
│   │   ├── core_piv_loop/      # 核心命令
│   │   └── validation/         # 验证命令
│   └── reference/              # 开发规范文档
├── .agents/                    # AI 工作流产物
│   ├── plans/                  # 实施计划
│   └── code-reviews/           # 代码审查记录
├── backend/                    # Spring Boot 后端
├── frontend/                   # Next.js 前端
├── python-services/            # FastAPI 网关
└── docs/                       # 项目文档
```

## 常见问题

### Q: AI 没有遵循规范怎么办？
A: 提醒 AI 阅读对应的 `*-best-practices.md`，例如：
```
请阅读 .claude/reference/spring-boot-best-practices.md 后重新实现
```

### Q: 计划不符合预期怎么办？
A: 在 `/piv:plan` 阶段明确指出需要调整的内容：
```
计划需要调整：
1. 不需要修改前端
2. 需要增加单元测试
```

### Q: 验证失败怎么办？
A: 根据错误信息修复，或让 AI 自动修复：
```bash
# 前端自动修复 lint
cd frontend && pnpm lint --fix
```

### Q: 如何新增功能模块？
A: 遵循各子项目的最佳实践：
- 后端：参考 `spring-boot-best-practices.md` 的 DDD 分层
- 前端：参考 `nextjs-best-practices.md` 的组件开发
- Python：参考 `fastapi-best-practices.md` 的模块化架构

## 最佳实践

1. **保持计划更新**：每次代码变更后更新 `.agents/plans/{任务}.md`
2. **小步提交**：每个功能或修复单独提交，便于追溯
3. **验证优先**：提交前确保 lint 和测试通过
4. **规范先行**：开发前先阅读对应的 best-practices.md
5. **文档同步**：新增 API 时更新 `.claude/reference/openapi-best-practices.md`

## 相关资源

- [PIV Loop 命令模板](./.claude/commands/)
- [开发规范文档](./.claude/reference/)
- [后端文档](./backend/CLAUDE.md)
- [前端文档](./frontend/CLAUDE.md)
- [Python 网关文档](./python-services/CLAUDE.md)
