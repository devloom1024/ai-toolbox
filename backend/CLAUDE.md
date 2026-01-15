# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是后端项目，基于 Spring Boot 4.0.1 + Java 21，采用**领域驱动设计 (DDD)** 分层架构。

**技术栈**:
- Spring Boot 4.0.1 + Java 21
- PostgreSQL 16 + Redis 7
- Flyway (数据库迁移)
- JWT (jjwt 0.11.5) + Spring Security
- Testcontainers (集成测试)
- Lombok + Jakarta Validation

## 快速开始

```bash
./mvnw spring-boot:run    # 启动开发环境 (自动启动 Docker Compose)
./mvnw test               # 运行所有测试
./mvnw test -Dtest=ClassName#methodName  # 运行单个测试
./mvnw clean package      # 编译打包
docker compose up -d      # 手动启动 PostgreSQL + Redis
```

## 架构设计

### 分层架构

```
com/devloom/ai/toolbox/
├── {domain}/              # 【业务领域】(如 auth)
│   ├── api/               # Controller - HTTP 请求处理
│   ├── service/           # Application Service - 业务编排
│   ├── domain/            # 领域层 (Entity/Repository/Enum/Converter)
│   ├── dto/               # 数据传输对象 (Request/Response)
│   └── infra/             # 基础设施层 (外部服务客户端)
│
└── common/                # 【跨领域公共组件】
    ├── config/            # 全局配置
    ├── security/          # 安全框架 (JWT/Filter/CurrentUser)
    ├── web/               # Web 过滤器 (DeviceContext/Trace)
    ├── exception/         # 异常处理 (BizException/GlobalExceptionHandler)
    └── response/          # 统一响应 (ApiResponse)
```

### 核心模块索引

| 模块 | 关键文件 | 核心功能 | 主要导出 |
|------|---------|---------|---------|
| **统一响应** | `common/response/ApiResponse.java` | 响应封装 | `success()`, `error()` |
| **安全配置** | `common/security/SecurityConfig.java` | Spring Security 配置 | Filter 链, 白名单 |
| **JWT 过滤器** | `common/security/JwtAuthenticationFilter.java` | Token 验证 | `doFilterInternal()` |
| **当前用户** | `common/security/CurrentUser.java` | 用户上下文 (ThreadLocal) | `getUserId()`, `getUsername()`, `set()` |
| **JWT 工具** | `common/security/JwtTokenProvider.java` | Token 生成/解析 | `generateToken()`, `validateToken()` |
| **设备上下文** | `common/web/DeviceContextHolder.java` | 设备标识 (ThreadLocal) | `getDeviceId()`, `set()` |
| **设备过滤器** | `common/web/DeviceContextFilter.java` | X-Device-Id 拦截 | `doFilterInternal()` |
| **全局异常** | `common/exception/GlobalExceptionHandler.java` | 异常处理 | `@ExceptionHandler` 方法 |
| **业务异常** | `common/exception/BizException.java` | 业务异常 | `BizException(BizErrorCode)` |
| **错误码** | `common/exception/BizErrorCode.java` | 错误码枚举 | 各业务错误码 |
| **国际化** | `common/config/MessageSourceConfig.java` | MessageSource 配置 | `messageSource()` |
| **认证服务** | `auth/service/AuthService.java` | 登录/注册/登出 | `login()`, `register()`, `logout()` |
| **Token 服务** | `auth/service/TokenService.java` | Token 生命周期管理 | `createTokens()`, `invalidateTokens()` |
| **OAuth 服务** | `auth/service/LinuxDoOAuthService.java` | LinuxDo OAuth | `authorize()`, `callback()` |
| **认证 API** | `auth/api/AuthController.java` | 认证接口 | 9 个认证端点 |
| **用户 Entity** | `auth/domain/entity/UserEntity.java` | 用户实体 | JPA 映射 |
| **登录记录 Entity** | `auth/domain/entity/LoginRecordEntity.java` | 登录记录实体 | JPA 映射 |
| **用户 Repository** | `auth/domain/repository/UserRepository.java` | 用户数据访问 | Spring Data JPA 方法 |

> 💡 **学习方法**: 阅读上述文件的源码，理解实际实现。所有关键类都有详细的 Javadoc 注释。

### 核心架构原理

#### 1. 统一响应封装架构

**流程**:
```
Controller 返回 ApiResponse<T>
  ↓
正常: ApiResponse.success(data) → { code: 200, message: "成功", data: T }
异常: GlobalExceptionHandler 捕获 → { code: 错误码, message: "错误信息", traceId: "xxx" }
```

**关键文件**: `common/response/ApiResponse.java`, `common/exception/GlobalExceptionHandler.java`

**实际使用**: 参考 `auth/api/AuthController.java`

**规则**:
- ✅ 所有 Controller 方法必须返回 `ApiResponse<T>`
- ❌ 禁止直接返回 `ResponseEntity` 或裸 DTO

#### 2. 设备上下文架构

**流程**:
```
HTTP 请求 → DeviceContextFilter (优先级 -100)
  ↓
读取 X-Device-Id Header → DeviceContextHolder.set(deviceId)
  ↓
Controller 通过 DeviceContextHolder.getDeviceId() 获取
  ↓
finally 块清理 ThreadLocal
```

**关键文件**: `common/web/DeviceContextFilter.java`, `common/web/DeviceContextHolder.java`

**实际使用**: 参考 `auth/service/AuthService.java` 中的 `login()` 方法

**规则**:
- ✅ 统一通过 `DeviceContextHolder.getDeviceId()` 获取
- ❌ 禁止在 Controller 中使用 `@RequestHeader("X-Device-Id")`

#### 3. 当前用户上下文架构

**流程**:
```
HTTP 请求 → JwtAuthenticationFilter (优先级 -99)
  ↓
验证 Bearer Token → 解析 userId/username
  ↓
CurrentUser.set(userId, username) → ThreadLocal 存储
  ↓
Controller/Service 通过 CurrentUser.getUserId() 获取
  ↓
finally 块清理 ThreadLocal
```

**关键文件**: `common/security/JwtAuthenticationFilter.java`, `common/security/CurrentUser.java`

**实际使用**: 参考 `auth/api/AuthController.java` 中的 `getProfile()` 方法

**规则**:
- ✅ 使用 `CurrentUser.getUserId()` 获取当前用户 ID
- ❌ 不要在 Controller 参数中传递 userId

#### 4. 事务管理架构

**关键文件**: `auth/service/AuthService.java`

**规则**:
- ✅ 必须显式声明 `@Transactional(rollbackFor = {Exception.class, Error.class})`
- ❌ 严禁通过 `this` 调用同一 Bean 内的 `@Transactional` 方法 (事务代理失效)

**解决方案**: 注入自身代理或抽取到独立组件

#### 5. Token 生命周期架构

**流程**:
```
登录成功 → TokenService.createTokens()
  ↓
生成 accessToken/refreshToken → 保存到 Redis (SET key value EX 7200)
  ↓
调用 flush() 强制写入 Redis → 立即可用
  ↓
登出 → TokenService.invalidateTokens() → Redis DEL
```

**关键文件**: `auth/service/TokenService.java`

**实际使用**: 参考 `auth/service/AuthService.java` 中的 `login()` 和 `logout()` 方法

**为什么需要 flush()**:
- Spring Data Redis 默认批量写入，`flush()` 强制立即持久化
- 避免登录后立即访问其他接口时 Token 尚未生效

#### 6. 异常处理架构

**流程**:
```
Service 抛出 BizException(BizErrorCode.USER_NOT_FOUND)
  ↓
GlobalExceptionHandler 捕获
  ↓
从 MessageSource 读取国际化消息
  ↓
返回 ApiResponse.error(code, message, traceId)
```

**关键文件**:
- `common/exception/BizException.java` - 业务异常基类
- `common/exception/BizErrorCode.java` - 错误码枚举
- `common/exception/GlobalExceptionHandler.java` - 全局异常处理器

**实际使用**: 参考 `auth/service/AuthService.java` 中的异常抛出

**国际化消息**: `src/main/resources/messages/messages*.properties`

#### 7. 国际化架构

**流程**:
```
HTTP 请求 → 读取 Accept-Language Header
  ↓
AcceptHeaderLocaleResolver 解析 Locale (zh-CN / en-US)
  ↓
MessageSource 从 messages_zh_CN.properties 或 messages.properties 读取
  ↓
GlobalExceptionHandler 使用 messageSource.getMessage(code, args, locale)
```

**关键文件**: `common/config/MessageSourceConfig.java`

**实际使用**: 参考 `common/exception/GlobalExceptionHandler.java`

**规则**:
- 所有用户可见消息必须从 `messages/*.properties` 读取
- Bean Validation 的 `message` 必须引用国际化资源键: `@NotBlank(message = "{validation.email.required}")`
- 所有枚举必须有注释说明语义

#### 8. OAuth 集成架构

**流程**:
```
前端调用 /api/v1/auth/oauth/linuxdo/authorize
  ↓
后端生成 state → 存储到 ConcurrentHashMap (30 分钟过期)
  ↓
返回 LinuxDoAuthorizeResponse (state + authorizeUrl)
  ↓
前端跳转到 authorizeUrl → 用户授权
  ↓
LinuxDo 回调到前端 → 前端调用 /api/v1/auth/oauth/linuxdo/callback
  ↓
后端验证 state → 用 code 换取 access_token → 获取用户信息 → 创建/登录用户
```

**关键文件**: `auth/service/LinuxDoOAuthService.java`

**实际使用**: 参考 `auth/api/AuthController.java` 中的 OAuth 端点

**为什么不使用 302 重定向**:
- 前端需要控制页面跳转逻辑
- 前端 SPA 架构更适合主动跳转

## 常见开发任务

### 添加新 API 端点

**参考文件**: `auth/api/AuthController.java`, `auth/service/AuthService.java`

1. 在 `{domain}/dto/request/` 创建请求 DTO (以 `Request` 结尾)
2. 在 `{domain}/dto/response/` 创建响应 DTO (以 `Response` 结尾)
3. 在 `{domain}/service/` 中实现业务逻辑
4. 在 `{domain}/api/` 中添加 Controller 方法，返回 `ApiResponse<T>`
5. 添加 Jakarta Validation 注解 (`@NotBlank`, `@Email` 等)
6. 在 `.claude/reference/openapi-best-practices.md` 中更新 API 文档

### 处理业务异常

**参考文件**: `common/exception/BizErrorCode.java`, `auth/service/AuthService.java`

1. 在 `BizErrorCode` 枚举中添加错误码
2. 在 `messages/*.properties` 中添加国际化消息
3. 在 Service 中抛出异常: `throw new BizException(BizErrorCode.USER_NOT_FOUND)`
4. `GlobalExceptionHandler` 会自动捕获并返回统一格式

### 添加数据库表

**参考文件**: `src/main/resources/db/migration/V1__init_auth_schema.sql`, `auth/domain/entity/UserEntity.java`

1. 创建 Flyway 迁移脚本: `db/migration/V{版本号}__{描述}.sql`
2. 编写 DDL 语句 (注意 PostgreSQL 特有类型)
3. 在 `{domain}/domain/entity/` 创建对应的 JPA Entity
4. 字段注释必须与 DDL 中的 COMMENT 保持一致
5. PostgreSQL 特有类型需要添加 `@JdbcTypeCode` 或自定义 `AttributeConverter`
6. **禁止修改已应用的迁移脚本**，新变更需创建新版本

### 编写集成测试

**参考文件**: `src/test/java/com/devloom/ai/toolbox/TestcontainersConfiguration.java`

1. 使用 `@SpringBootTest` 启动完整应用上下文
2. 使用 `@Testcontainers` 自动启动 PostgreSQL/Redis 容器
3. 测试完整业务流程 (Controller → Service → Repository)
4. 使用 `@Transactional` 自动回滚测试数据

## 开发规范

### 目录结构规范

为保持目录结构清晰，便于后续代码组织，DDD 分层目录即使为空也应保留。

1. **空目录处理**: 空目录应添加 `.gitkeep` 文件，确保目录结构被 Git 追踪
   ```
   src/main/java/com/devloom/ai/toolbox/{domain}/
   ├── api/              # Controller 层
   ├── service/          # 应用服务层
   ├── domain/           # 领域层 (entity/repository/enums/converter)
   ├── dto/              # 数据传输层 (request/response)
   └── infra/            # 基础设施层
   ```

2. **禁止删除空目录**: 即使当前没有实现，也应保留目录结构并添加 `.gitkeep`
   - 便于后续功能扩展时知道在哪里添加代码
   - 保持项目结构的可预见性
   - 避免多人协作时目录结构混乱

3. **创建 .gitkeep**: 在空目录中创建空文件
   ```bash
   # 方式一：手动创建
   touch src/main/java/.../domain/entity/.gitkeep

   # 方式二：批量创建
   find . -type d -empty -exec touch {}/.gitkeep \; -o -type d -empty -exec mkdir -p {}/.gitkeep \;
   ```

### 代码注释规范

1. **类注释**: 每个类必须有 `@author` 注解，值为 Git 配置的 username
   ```java
   /**
    * 用户认证服务
    *
    * @author your-git-username
    */
   @Service
   public class AuthService {
   }
   ```

2. **关键位置注释**: 在以下位置必须添加适当的注释
   - 复杂业务逻辑的关键步骤
   - 非显而易见的技术决策（如 flush() 的使用）
   - 重要的业务规则和约束
   - 临时解决方案或已知问题（使用 TODO/FIXME）

3. **方法注释**: Controller 的每个接口方法必须有 Javadoc 说明接口用途

### Controller 层

1. **返回类型**: 必须返回 `ApiResponse<T>`
2. **设备标识**: 从 `DeviceContextHolder` 获取，不要使用 `@RequestHeader`
3. **当前用户**: 从 `CurrentUser` 获取，不要在参数中传递
4. **参数校验**: 使用 `@Valid` + Jakarta Validation 注解

### DTO 层

1. **API 层 DTO 命名规范** (与 `.claude/reference/openapi-best-practices.md` 保持一致):
   - 请求 DTO: `*Request` (如 `LoginRequest`)
   - 响应 DTO: `*Response` (如 `TokenResponse`)
2. **基础设施层 DTO 命名规范** (适配器内部使用):
   - 请求 DTO: `*Command` (如 `AkShareQuoteCommand`)
   - 响应 DTO: `*Result` (如 `AkShareQuoteResult`)
3. **禁止事项**:
   - ❌ Infra 层 DTO 禁止使用 `*Request/*Response` 后缀 (与 API 层冲突)
   - ❌ 禁止使用 `@Builder` 注解，Java Bean 默认使用 `@Data` 注解
4. **字段注释**: 所有字段必须有 Javadoc 注释
5. **校验注解**: Bean Validation 的 `message` 必须引用国际化资源键

### Entity 层

1. **字段注释**: 必须与 `db/migration/*.sql` 中的 DDL 保持一致
2. **PostgreSQL 特有类型**:
   - `INET` → `@JdbcTypeCode(SqlTypes.INET)`
   - `JSONB` → `@JdbcTypeCode(SqlTypes.JSON)`
   - `SMALLINT` 枚举 → `@Convert(converter = XxxConverter.class)`
3. **修改 Entity 后必须对照 Flyway 脚本进行 schema 校验**

### Service 层

1. **事务声明**: 必须显式声明 `rollbackFor = {Exception.class, Error.class}`
2. **事务代理**: 避免通过 `this` 调用同一 Bean 内的 `@Transactional` 方法
3. **业务逻辑**: 复杂业务逻辑拆分为多个 private 方法
4. **异常抛出**: 手动抛出异常时必须使用 `BizException`，不要使用通用异常类

### Repository 层

使用 Spring Data JPA，继承 `JpaRepository<Entity, ID>`。参考 `auth/domain/repository/UserRepository.java`。

### 方法参数规范

当方法参数超过 **3 个**时，必须封装为独立的 Java Bean，避免长参数列表。

**适用场景**:
- Controller 的 Query Parameters
- Service 层的复杂查询条件
- Service 层的批量操作参数

**命名规则**:
- 查询参数: `*Query` 或 `*Request` (如 `GetWatchlistRequest`)
- 操作命令: `*Command` (如 `LoginAuditCommand`)
- 上下文对象: `*Context` (如 `LoginAuditContext`)

**示例**:

```java
// ❌ 错误 - 5 个参数
public WatchlistDataResponse getWatchlist(Long userId, Long groupId,
        MarketType market, Integer page, Integer size) { }

// ✅ 正确 - 封装为查询对象
public WatchlistDataResponse getWatchlist(Long userId, GetWatchlistRequest query) {
    // query 包含 groupId, market, page, size
}
```

```java
// ❌ 错误 - 6 个参数
private void recordLogin(UserEntity user, IdentityType type,
        String identifier, String ip, String userAgent, LoginStatus status) { }

// ✅ 正确 - 封装为上下文对象
private void recordLogin(UserEntity user, LoginAuditContext ctx) {
    // ctx 包含 type, identifier, ip, userAgent, status
}
```

### 分页查询规范

项目提供统一的分页基类，支持快速实现分页查询。

#### 分页请求基类

**关键文件**: `common/dto/PageRequest.java`

```java
// 继承 PageRequest 获取分页参数处理能力
@Getter
public class GetWatchlistRequest extends PageRequest {
    private Long groupId;      // 查询条件
    private MarketType market; // 查询条件
}
```

**分页参数**:
| 字段 | 类型 | 说明 |
|-----|------|------|
| `page` | Integer | 页码，从 1 开始，默认 1 |
| `size` | Integer | 每页大小，默认 20 |

**工具方法**:
- `getPageZeroBased()`: 获取从 0 开始的页码（用于 Spring Data JPA）
- `getPageSize()`: 获取每页大小
- `getPageOneBased()`: 获取从 1 开始的页码

#### 分页响应基类

**关键文件**: `common/dto/PageResponse.java`

```java
// 继承 PageResponse 获取分页信息
@Getter
public class WatchlistDataResponse extends PageResponse {
    private List<WatchlistItemResponse> items; // 业务数据
}
```

**响应字段**:
| 字段 | 类型 | 说明 |
|-----|------|------|
| `page` | Integer | 当前页码（从 1 开始） |
| `size` | Integer | 每页大小 |
| `total` | Long | 总记录数 |
| `pages` | int | 总页数（计算属性） |

**判断方法**:
- `hasPrevious()`: 是否有上一页
- `hasNext()`: 是否有下一页
- `isFirst()`: 是否为第一页
- `isLast()`: 是否为最后一页

#### 使用示例

```java
// Controller 层
@GetMapping
public ApiResponse<WatchlistDataResponse> getWatchlist(
        @AuthenticationPrincipal CurrentUser currentUser,
        @ModelAttribute GetWatchlistRequest query) {
    WatchlistDataResponse result = watchlistService.getWatchlist(currentUser.getUserId(), query);
    return ApiResponse.success(result);
}

// Service 层
@Transactional(readOnly = true, rollbackFor = {Exception.class, Error.class})
public WatchlistDataResponse getWatchlist(Long userId, GetWatchlistRequest request) {
    WatchlistQuery query = WatchlistQuery.builder()
            .groupId(request.getGroupId())
            .market(request.getMarket())
            .page(request.getPage())
            .size(request.getSize())
            .build();

    PageRequest pageRequest = PageRequest.of(
            query.getPageZeroBased(),
            query.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
    );

    Page<Entity> entityPage = repository.findAllByUserId(userId, pageRequest);

    return WatchlistDataResponse.superBuilder()
            .items(entityPage.getContent().stream().map(this::toDto).collect(toList()))
            .total(entityPage.getTotalElements())
            .page(query.getPageOneBased())
            .size(query.getPageSize())
            .build();
}
```

### 禁止事项

#### ❌ 直接返回 ResponseEntity

```java
// ❌ 错误
public ResponseEntity<TokenResponse> login() { }

// ✅ 正确
public ApiResponse<TokenResponse> login() { }
```

#### ❌ 直接读取 Header

```java
// ❌ 错误
public void method(@RequestHeader("X-Device-Id") String deviceId) { }

// ✅ 正确
public void method() {
    String deviceId = DeviceContextHolder.getDeviceId();
}
```

#### ❌ 使用通用异常类

```java
// ❌ 错误 - 不要使用通用异常
throw new RuntimeException("用户不存在");
throw new IllegalArgumentException("无效参数");
throw new Exception("操作失败");

// ✅ 正确 - 使用 BizException
throw new BizException(BizErrorCode.USER_NOT_FOUND);
throw new BizException(BizErrorCode.INVALID_PARAMETER);
throw new BizException(BizErrorCode.OPERATION_FAILED);
```

#### ❌ 硬编码消息

```java
// ❌ 错误
throw new BizException(40001, "用户不存在");

// ✅ 正确
throw new BizException(BizErrorCode.USER_NOT_FOUND);
```

#### ❌ 事务方法内部调用

```java
// ❌ 错误 - 事务不会生效
@Service
public class MyService {
    public void methodA() {
        this.methodB(); // 事务失效!
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void methodB() { }
}
```

#### ❌ 修改已应用的 Flyway 脚本

```bash
# ❌ 错误 - 不要修改已存在的迁移脚本
V1__init_auth_schema.sql  # 已应用到数据库

# ✅ 正确 - 创建新版本
V2__add_user_avatar.sql
```

## 关键设计决策

### 为什么使用 DDD 分层架构？

**优势**:
- ✅ 清晰的职责分离 (Controller/Service/Repository)
- ✅ 业务逻辑与基础设施解耦
- ✅ 易于测试和维护
- ✅ 适合中大型项目扩展

**权衡**:
- ❌ 增加代码层次和文件数量
- ❌ 学习曲线较陡

### 为什么使用 DeviceContextFilter？

**问题**: 多个接口需要读取 `X-Device-Id`，每次都写 `@RequestHeader` 重复且难以维护。

**解决方案**: 使用 Filter 统一拦截并存储到 ThreadLocal，Controller 通过 Holder 获取。

**优势**:
- ✅ 代码简洁，无需每个方法都声明参数
- ✅ 统一处理缺失/无效设备 ID 的情况
- ✅ 易于扩展 (如记录日志、限流)

### 为什么使用 flush() 强制持久化 Token？

**问题**: Spring Data Redis 默认批量写入，登录后立即访问其他接口可能 Token 尚未生效。

**解决方案**: 在 `TokenService.createTokens()` 后调用 `redisTemplate.execute()` 手动 `flush()`。

**优势**:
- ✅ 确保 Token 立即可用
- ✅ 避免时序问题导致的 401 错误

参考 `auth/service/TokenService.java:62-64`。

## 环境变量管理

所有环境变量统一在 `backend/.env` 中定义，Spring Boot 和 Docker Compose 共用该文件。

**修改环境变量时**:
1. 同步更新 `.env.example`
2. 在 PR 中说明变更原因

## 数据库迁移

使用 **Flyway** 管理数据库版本:
- 迁移脚本位于 `src/main/resources/db/migration/`
- 命名规范: `V{版本号}__{描述}.sql` (如 `V1__init_auth_schema.sql`)
- **禁止修改已应用的迁移脚本**，新变更需创建新版本脚本

## 注意事项

- **事务陷阱**: Spring AOP 代理机制要求跨方法调用才能触发事务，内部调用需注入自身代理或抽取独立组件
- **Flyway 校验**: 修改 Entity 后务必对照迁移脚本进行 schema 校验再提交
- **DTO 命名**: 严格遵守 `*Request/*Response` 后缀规范，与 OpenAPI Schema 保持一致
- **国际化**: 所有用户可见消息必须支持国际化，不要硬编码中英文文本
- **Token 持久化**: 使用 Redis 存储 Token 时记得调用 `flush()` 确保立即生效
