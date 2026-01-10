# AI Toolbox 项目规范

本项目是一个全栈 monorepo，包含 frontend (Next.js 16) 和 backend (Spring Boot 4.0.1) 两个子项目。

## 项目结构

```
ai-toolbox/
├── frontend/          # Next.js 16 + TypeScript + Tailwind CSS + shadcn/ui
├── backend/           # Spring Boot 4.0.1 + Java 21 + PostgreSQL + Redis
├── docs/              # 设计文档和 API 规范
└── target/            # 构建产物
```

## 开发环境

- **Node.js**: pnpm 10.19.0
- **Java**: JDK 21
- **数据库**: PostgreSQL + Redis (通过 Docker Compose 启动)
- **包管理**: pnpm (frontend), Maven (backend)

## 常用命令

```bash
# 前端开发
cd frontend && pnpm dev
pnpm lint                          # ESLint 检查
pnpm build                         # 生产构建

# 后端开发
cd backend && ./mvnw spring-boot:run
./mvnw test                        # 运行测试
./mvnw test -Dtest=ClassName       # 运行单个测试类
./mvnw compile                     # 编译
```

## 代码风格总则

1. **提交前检查**: 提交前确保 lint 和测试通过
2. **国际化**: 所有用户可见文本必须支持中英文国际化
3. **分支管理**: 功能开发使用 `feature/*` 分支，从 main 分支创建
4. **Commit Message**: 遵循 Angular 规范 (feat/fix/docs/refactor/test/chore)

## 更多信息

- [Frontend 规范](./frontend/AGENTS.md)
- [Backend 规范](./backend/AGENTS.md)
- [API 规范](./docs/specs/api.md)
- [数据库设计](./docs/specs/postgresql.md)
