# Spring Boot 最佳实践

## 项目配置

- **Spring Boot**: 4.0.1
- **Java**: 21
- **包管理器**: Maven
- **数据库**: PostgreSQL 16
- **缓存**: Redis 7

## DDD 分层架构

```
com/devloom/ai/toolbox/
├── {domain}/                    # 业务领域模块
│   ├── api/                     # Controller 层
│   │   └── *Controller.java
│   ├── service/                 # 应用服务层
│   │   └── *Service.java
│   ├── domain/                  # 领域层
│   │   ├── entity/              # 实体
│   │   ├── repository/          # 仓储接口
│   │   ├── enum/                # 枚举
│   │   └── converter/           # 转换器
│   ├── dto/                     # 数据传输对象
│   │   ├── request/             # *Request.java
│   │   └── response/            # *Response.java
│   └── infra/                   # 基础设施层
│       └── *Client.java
└── common/                      # 公共模块
    ├── config/
    ├── security/
    ├── web/
    ├── exception/
    └── response/
```

## 核心规范

### 统一响应
```java
@RestController
public class ExampleController {
    @GetMapping("/api/v1/example")
    public ApiResponse<ExampleResponse> getExample() {
        return ApiResponse.success(service.getExample());
    }
}
```

### 设备上下文
```java
// ❌ 错误
public void method(@RequestHeader("X-Device-Id") String deviceId) { }

// ✅ 正确
public void method() {
    String deviceId = DeviceContextHolder.getDeviceId();
}
```

### 当前用户
```java
// ❌ 错误
public UserResponse getUser(Long userId) { }

// ✅ 正确
public UserResponse getProfile() {
    Long userId = CurrentUser.getUserId();
    return service.getUser(userId);
}
```

### 事务管理
```java
@Service
public class UserService {
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateUser(UserUpdateRequest request) {
        // 业务逻辑
    }
}
```

### 业务异常
```java
// 抛出异常
throw new BizException(BizErrorCode.USER_NOT_FOUND);

// 错误码定义
public enum BizErrorCode {
    USER_NOT_FOUND(40001, "用户不存在"),
    INVALID_PARAMETER(40002, "无效参数");

    private final int code;
    private final String message;
}
```

### 国际化消息
```java
// message.properties
error.user.not-found=用户不存在

// 使用
throw new BizException(BizErrorCode.USER_NOT_FOUND);
```

## 数据库迁移 (Flyway)

```sql
-- V1__init_auth_schema.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID';
```

## 测试 (Testcontainers)

```java
@SpringBootTest
@Testcontainers
class AuthServiceTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
        .withExposedPorts(6379);
}
```

## 禁止事项

- ❌ 直接返回 `ResponseEntity`
- ❌ 硬编码错误消息
- ❌ 修改已应用的 Flyway 脚本
- ❌ 事务方法内部调用 (使用 `this`)
- ❌ 使用通用异常类 (`RuntimeException`)
