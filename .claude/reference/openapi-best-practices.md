# OpenAPI 规范

## 概述

本文档定义了项目中 OpenAPI 文件的编写规范，确保 API 文档的一致性和可维护性。

## Model 定义规范

### 1. 请求参数 Model 命名

所有请求参数 Model **必须**以 `Request` 结尾，保持与后端 DTO 命名一致。

**示例：**
```yaml
components:
  schemas:
    RegisterRequest:
      type: object
      required: [email, password, code, nickname]
      properties:
        email:
          type: string
          format: email
        password:
          type: string
          format: password

    LoginRequest:
      type: object
      required: [identifier, password]
      properties:
        identifier:
          type: string
        password:
          type: string
```

### 2. 响应参数 Model 命名

所有响应数据 Model **必须**以 `Response` 结尾。

**示例：**
```yaml
components:
  schemas:
    RegisterResponse:
      type: object
      properties:
        userId:
          type: integer
          format: int64

    TokenResponse:
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

统一的 ApiResponse **必须**为 `ApiResponse`，定义基础的响应结构。

```yaml
components:
  schemas:
    ApiResponse:
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
        traceId:
          type: string
          nullable: true
          description: 请求链路追踪 ID，用于问题排查
```

#### 3.2 ApiResponse 中的 data 类型规范

**关键规则**：`ApiResponse<T>` 中的 `data` 字段引用的 Model **必须**以 `Response` 结尾。

**正确示例：**
```yaml
ApiResponseSearchResult:
  allOf:
    - $ref: '#/components/schemas/ApiResponse'
    - type: object
      properties:
        data:
          $ref: '#/components/schemas/SearchResultResponse'  # ✅ 以 Response 结尾

SearchResultResponse:  # ✅ 以 Response 结尾
  type: object
  properties:
    securities:
      type: array
      items:
        $ref: '#/components/schemas/SecuritySearchItem'
```

**错误示例：**
```yaml
ApiResponseSearchResult:
  allOf:
    - $ref: '#/components/schemas/ApiResponse'
    - type: object
      properties:
        data:
          $ref: '#/components/schemas/SearchResultResponse'  # ❌ 未以 Response 结尾
```

**命名规则总结：**
| Model 类型 | 命名规则 | 示例 |
|-----------|---------|------|
| ApiResponse 包装响应 | `ApiResponse` + 业务概念 + `Result`/`Data` | `ApiResponseSearchResult`, `ApiResponseWatchlistData` |
| 实际数据 Model | **必须以 `Response` 结尾** | `SearchResultResponse`, `WatchlistDataResponse` |
| 列表项 Model | 不以 `Response` 结尾 | `SecuritySearchItem`, `WatchlistItem` |

#### 3.3 错误响应

所有错误相关的 Model **必须**以 `ApiResponse` 开头，然后添加具体的错误类型描述。

**示例：**
```yaml
components:
  schemas:
    ApiResponseError:
      allOf:
        - $ref: '#/components/schemas/ApiResponse'
      example:
        code: 2004
        message: password error
        data: null

    ApiResponseValidationError:
      allOf:
        - $ref: '#/components/schemas/ApiResponse'
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

#### 3.4 成功响应（带数据）

成功响应可以自由命名，但建议使用 `ApiResponse` + 业务概念的格式。

**示例：**
```yaml
components:
  schemas:
    ApiResponseRegister:
      allOf:
        - $ref: '#/components/schemas/ApiResponse'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/RegisterResponse'

    ApiResponseToken:
      allOf:
        - $ref: '#/components/schemas/ApiResponse'
        - type: object
          properties:
            data:
              $ref: '#/components/schemas/TokenResponse'
```

## 命名规范总结

| Model 类型 | 命名规则 | 示例 |
|-----------|---------|------|
| 请求参数 | 必须以 `Request` 结尾 | `RegisterRequest`, `LoginRequest` |
| 响应数据 | 必须以 `Response` 结尾 | `RegisterResponse`, `TokenResponse` |
| 基础响应 | 必须为 `ApiResponse` | `ApiResponse` |
| 错误响应 | 必须以 `ApiResponse` 开头 | `ApiResponseError`, `ApiResponseValidationError` |
| 成功响应 | 建议以 `ApiResponse` 开头 | `ApiResponseRegister`, `ApiResponseToken` |

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
              $ref: '#/components/schemas/RegisterRequest'
      responses:
        '201':
          description: 注册成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ApiResponseRegister'
        '400':
          description: 参数错误
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ApiResponseError'
```

## 版本历史

- **v1.0.0** (2026-01-06): 初始版本，定义 Model 命名规范
