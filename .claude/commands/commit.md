# /piv:commit - 智能提交

基于 Angular 规范生成规范的 Git Commit Message，自动匹配现有提交语言风格。

## 使用方式

直接描述你想要提交的变更，例如:
- "添加用户登录功能"
- "修复认证模块的 Token 刷新 bug"
- "重构股票搜索服务"

## 执行步骤

### 1. 检测提交语言
```bash
# 分析最近 5 条提交，判断语言风格
git log --oneline -5

# 检测示例:
# - 中文: "feat(auth): 添加用户登录功能" → 使用中文生成
# - 英文: "feat(auth): add user login feature" → 使用英文生成
```

**语言检测规则**:
- 如果最近 3 条提交 majority 使用中文 → 生成中文 commit message
- 如果最近 3 条提交 majority 使用英文 → 生成英文 commit message
- 如果无法判断，默认使用中文（根据项目全局设置）

### 2. 分析变更
```bash
git status              # 查看所有变更
git diff --staged       # 查看已暂存变更
git diff                # 查看未暂存变更
```

### 3. 生成提交信息

根据变更内容和检测到的语言，生成符合 Angular 规范的提交信息：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 4. 类型规范

| 类型 | 说明 | 中文示例 | 英文示例 |
|------|------|---------|---------|
| feat | 新功能 | `feat(auth): 添加用户登录功能` | `feat(auth): add user login feature` |
| fix | 修复 bug | `fix(auth): 修复 Token 刷新失败问题` | `fix(auth): fix token refresh failure` |
| refactor | 重构 | `refactor(investment): 重命名领域实体` | `refactor(investment): rename domain entities` |
| docs | 文档更新 | `docs: 更新 API 规范文档` | `docs: update API specification` |
| style | 代码格式 | `style(frontend): 格式化登录组件` | `style(frontend): format login component` |
| test | 测试相关 | `test(auth): 添加登录集成测试` | `test(auth): add login integration test` |
| chore | 构建/工具 | `chore: 更新依赖版本` | `chore: update dependency versions` |
| perf | 性能优化 | `perf(api): 优化股票查询性能` | `perf(api): optimize stock query performance` |

### 5. Scope 规范

使用受影响的子项目或模块作为 scope:

- `backend`, `frontend`, `python-services`
- `auth`, `investment`, `user` (后端模块)
- `api`, `components`, `lib` (前端模块)

### 6. 生成 Commit

```bash
# 暂存变更
git add <files>

# 创建提交 (会自动填充生成的 message)
git commit
```

## 输出格式

### 中文格式
```markdown
feat(auth): 添加用户登录功能

- 实现用户名密码登录
- 添加 JWT Token 生成
- 集成 Redis Token 存储
- 添加登录日志记录

Close #123
```

### 英文格式
```markdown
feat(auth): add user login feature

- Implement username/password login
- Add JWT token generation
- Integrate Redis token storage
- Add login audit logging

Close #123
```

## 验证

提交后检查:
```bash
git log -3 --oneline    # 查看提交历史
```

## 语言切换

如果需要临时切换语言，可以在描述前指定：

```
/piv:commit [EN] 添加用户登录功能  # 强制使用英文
/piv:commit [CN] add user login feature  # 强制使用中文
```
