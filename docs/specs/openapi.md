# OpenAPI 规范

## 概述

本文档定义了项目中 OpenAPI 文件的编写规范，确保 API 文档的一致性和可维护性。

## Model 定义规范

### 1. 请求参数 Model 命名

所有请求参数 Model **必须**以 `Param` 结尾。

**示例：**
```yaml
components:
  schemas:
    RegisterParam:
      type: object
      required: [email, password, code, nickname]
      properties:
        email:
          type: string
          format: email
        password:
          type: string
          format: password

    LoginParam:
      type: object
      required: [identifier, password]
      properties:
        identifier:
          type: string
        password:
          type: string
```

### 2. 响应参数 Model 命名

所有响应数据 Model **必须**以 `Result` 结尾。

**示例：**
```yaml
components:
  schemas:
    RegisterResult:
      type: object
      properties:
        userId:
          type: integer
          format: int64

    TokenPairResult:
      type: object
      properties:
        accessToken:
          type: string
        refreshToken:
          type: string
        expiresIn:
          type: integer
```

### 3. ApiResponse 规范

#### 3.1 基础响应结构

统一的 ApiResponse **必须**为 `BaseResult`，定义基础的响应结构。

```yaml
components:
  schemas:
    BaseResult:
      type: object
      required: [code, message]
      properties:
        code:
          type: integer
          example: 0
          description: 业务状态码，0 表示成功
        message:
          type: string
          example: ok
          description: 响应消息
        data:
          nullable: true
          description: 响应数据，可为 null
```

#### 3.2 错误响应

所有错误相关的 Model **必须**以 `BaseResult` 开头，然后添加具体的错误类型描述。

**示例：**
```yaml
components:
  schemas:
    BaseResultError:
      allOf:
        - $ref: '#/components/schemas/BaseResult'
      example:
        code: 2004
        message: password error
        data: null

    BaseResultValidationError:
      allOf:
        - $ref: '#/components/schemas/BaseResult'
        - type: object
          properties:
            data:
              type: object
              properties:
                errors:
                  type: array
                  items:
                    type: object
                    properties:
                      field:
                        type: string
                      message:
                        type: string
```

#### 3.3 成功响应（带数据）

成功响应可以自由命名，但建议使用 `BaseResult` + 业务概念的格式。

**示例：**
```yaml
components:
  schemas:
    BaseResultRegister:
      allOf:
        - $ref: '#/components/schemas/BaseResult'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/RegisterResult'

    BaseResultToken:
      allOf:
        - $ref: '#/components/schemas/BaseResult'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/TokenPairResult'
```

## 命名规范总结

| Model 类型 | 命名规则 | 示例 |
|-----------|---------|------|
| 请求参数 | 必须以 `Param` 结尾 | `RegisterParam`, `LoginParam` |
| 响应数据 | 必须以 `Result` 结尾 | `RegisterResult`, `TokenPairResult` |
| 基础响应 | 必须为 `BaseResult` | `BaseResult` |
| 错误响应 | 必须以 `BaseResult` 开头 | `BaseResultError`, `BaseResultValidationError` |
| 成功响应 | 建议以 `BaseResult` 开头 | `BaseResultRegister`, `BaseResultToken` |

## 最佳实践

1. **一致性**：在整个项目中保持命名一致性，便于代码生成和理解
2. **语义化**：使用清晰、有意义的名称描述 Model 的用途
3. **避免缩写**：除非是行业通用缩写，否则使用完整单词
4. **单一职责**：每个 Model 应该只代表一个明确的概念

## 示例

完整的 API 定义示例：

```yaml
paths:
  /api/v1/auth/register:
    post:
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterParam'
      responses:
        '201':
          description: 注册成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BaseResultRegister'
        '400':
          description: 参数错误
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BaseResultError'
```

## 版本历史

- **v1.0.0** (2026-01-06): 初始版本，定义 Model 命名规范
