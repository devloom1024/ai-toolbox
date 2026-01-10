# Backend 项目规范

Spring Boot 4.0.1 + Java 21 后端 API 服务。

## 技术栈

- **框架**: Spring Boot 4.0.1
- **语言**: Java 21
- **数据库**: PostgreSQL + JPA + Flyway Migration
- **缓存**: Redis + Spring Data Redis
- **安全**: Spring Security + JWT + OAuth2 Client
- **构建**: Maven + GraalVM Native Support
- **测试**: JUnit 5 + Testcontainers

## 命令

```bash
cd backend

# 开发
./mvnw spring-boot:run           # 启动开发服务器
./mvnw compile                   # 编译
./mvnw test                      # 运行所有测试
./mvnw test -Dtest=ClassName     # 运行单个测试类
./mvnw test -Dtest=ClassName#methodName  # 运行单个测试方法
./mvnw package -DskipTests       # 构建 JAR

# Docker
docker compose up -d             # 启动 PostgreSQL + Redis
```

## 目录结构

```
backend/src/main/java/com/devloom/ai/toolbox/
├── common/               # 公共模块
│   ├── config/          # 配置类 (Redis, RestTemplate, Time, MessageSource)
│   ├── exception/       # 异常处理 (BizException, GlobalExceptionHandler)
│   ├── response/        # ApiResponse<T> 统一响应
│   ├── security/        # JWT, Filter, Config
│   ├── util/            # 工具类
│   └── web/             # DeviceContextFilter, RequestHeaderExtractor
├── auth/                 # 认证模块
│   ├── api/             # AuthController
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA Entities
│   ├── infra/           # 基础设施 (MailClient, OAuthClient)
│   ├── repository/      # Spring Data Repositories
│   └── service/         # 业务逻辑
└── AiToolboxApplication.java

backend/src/main/resources/
├── application.yml           # 主配置
├── application-dev.yml       # 开发环境
├── application-prod.yml      # 生产环境
├── db/migration/             # Flyway SQL 脚本
└── messages/                 # 国际化资源文件
```

## 代码规范

### 命名规范

- **包名**: lowercase (com.devloom.ai.toolbox.xxx)
- **类名**: PascalCase (UserService, AuthController)
- **方法名**: camelCase (findById, saveUser)
- **常量**: UPPER_SNAKE_CASE
- **DTO**: `XxxRequest`, `XxxResponse` 命名
- **Entity**: 对应表名，可省略表名后缀

### 项目结构风格

- **Controller**: 只处理参数校验和响应封装，业务逻辑下放到 Service
- **Service**: 业务逻辑层，事务边界
- **Repository**: 数据访问层，只做 CRUD
- **Infra**: 外部服务封装 (Mail, OAuth API)
- **DTO**: 只承载数据，无业务逻辑

### Controller 规范

- 必须返回 `ApiResponse<T>`，禁止直接返回 `ResponseEntity` 或裸 DTO
- 使用 `@Valid` + `@RequestBody` 校验请求
- 设备标识由 `DeviceContextHolder` 获取，禁止使用 `@RequestHeader` 读取 `X-Device-Id`
- 每个方法必须编写注释说明用途

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 令牌响应
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse token = authService.login(request.getEmail(), request.getPassword());
        return ApiResponse.success(token);
    }
}
```

### Service 规范

- `@Transactional` 必须声明 `rollbackFor = {Exception.class, Error.class}`
- 禁止在同一个 Bean 内通过 `this` 调用另一个 `@Transactional` 方法
- 使用 `@Lazy` 或注入自身代理解决自调用问题

```java
@Service
@RequiredArgsConstructor
public class UserService {

    @Lazy
    private final UserService self;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = findById(id);
        // 业务逻辑...
        self.updateUserStatistics(id);  // 通过代理调用
    }
}
```

### DTO/Entity 规范

- 所有字段必须添加注释，与数据库表结构一致
- PostgreSQL 特有类型使用 `@JdbcTypeCode` 或 `columnDefinition`
- 枚举类型必须补充注释说明各枚举值含义

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户邮箱
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 用户状态
     * @see UserStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
}
```

### 异常处理

- 使用 `BizException` 抛出业务异常，携带 `BizErrorCode`
- 统一通过 `GlobalExceptionHandler` 处理
- 错误信息支持国际化，从 `messages/*.properties` 读取

```java
throw new BizException(BizErrorCode.USER_NOT_FOUND, userId);
```

### 导入顺序

```java
package com.devloom.ai.toolbox.xxx;

import java.xxx;
import lombok.xxx;
import jakarta.xxx;
import org.springframework.xxx;
import com.devloom.ai.toolbox.common.xxx;
```

### 国际化

- 所有用户可见 message 必须支持国际化
- Bean Validation 的 `message` 必须引用资源键
- 使用 `MessageSource` 获取本地化消息

```java
@NotBlank(message = "{validation.required}")
private String name;
```

### 测试规范

- 使用 `@SpringBootTest` + `@Testcontainers`
- 数据库使用 PostgreSQL Testcontainer
- 测试类放在 `src/test/java/` 对应目录
- 单元测试覆盖核心业务逻辑

### 其他规范

- 使用 Lombok 减少样板代码 (`@Getter`, `@Setter`, `@RequiredArgsConstructor`)
- 日志使用 `@Slf4j` + `log.error/warn/info/debug`
- 环境变量统一放在 `backend/.env`，同步更新 `.env.example`
- 新增/修改字段后务必对照 Flyway 脚本进行 schema 校验

