# /piv:validate - 验证代码质量

运行 lint、test 和 build 验证代码质量。

> **参考规范**:
> - [spring-boot-best-practices.md](../reference/spring-boot-best-practices.md)
> - [nextjs-best-practices.md](../reference/nextjs-best-practices.md)
> - [fastapi-best-practices.md](../reference/fastapi-best-practices.md)

## 执行步骤

### 1. 后端验证 (backend)

```bash
cd backend

# 代码检查
./mvnw spotless:check      # 代码格式检查

# 运行测试
./mvnw test               # 运行所有测试
./mvnw test -Dtest=ClassName#methodName  # 运行单个测试

# 构建
./mvnw clean package -DskipTests
```

**验证项**:
- ✅ Spotless 格式检查通过
- ✅ 单元测试通过
- ✅ 集成测试通过 (Testcontainers)
- ✅ JAR 包构建成功

### 2. 前端验证 (frontend)

```bash
cd frontend

# 代码检查
pnpm lint                 # ESLint 检查
pnpm lint --fix           # 自动修复

# 构建
pnpm build                # 静态导出到 out/
```

**验证项**:
- ✅ ESLint 检查通过
- ✅ TypeScript 编译成功
- ✅ 静态导出成功

### 3. Python 网关验证 (python-services)

```bash
cd python-services

# 代码检查
uv run ruff check gateway/           # ruff 检查
uv run ruff check --fix gateway/     # 自动修复
uv run ruff format --check gateway/  # 格式检查

# 类型检查
uv run mypy gateway/

# 测试
uv run pytest                        # 运行所有测试
uv run pytest gateway/tests/         # 指定目录

# 构建验证
uv run python run.py --help          # 启动验证
```

**验证项**:
- ✅ ruff 检查通过
- ✅ mypy 类型检查通过
- ✅ pytest 测试通过

### 4. 数据库迁移验证

```bash
cd backend

# 检查 Flyway 迁移脚本
./mvnw flyway:info                   # 查看迁移状态

# 确保迁移脚本命名规范: V{版本号}__{描述}.sql
```

### 5. 综合验证结果

```markdown
## 验证结果

### 后端
- [x] 格式检查: 通过
- [x] 单元测试: 通过 (X/Y)
- [x] 集成测试: 通过 (X/Y)
- [x] 构建: 成功

### 前端
- [x] ESLint: 通过
- [x] TypeScript: 通过
- [x] 构建: 成功

### Python
- [x] ruff: 通过
- [x] mypy: 通过
- [x] pytest: 通过 (X/Y)

## 问题清单

| 严重性 | 文件 | 问题 | 处理建议 |
|--------|------|------|---------|
| ERROR | path | 描述 | 建议 |
```

## 失败处理

- **Lint 失败**: 运行 `--fix` 自动修复，或手动修复
- **测试失败**: 分析失败原因，修复后重新运行
- **构建失败**: 检查依赖和配置