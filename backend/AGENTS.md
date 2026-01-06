# 后端 Agent 规范

- Controller 必须返回 `ApiResponse<T>`，禁止直接返回 `ResponseEntity` 或裸 DTO，必要时请新增 DTO 或调整服务层。
- 设备标识（如 `X-Device-Id`）统一由 `DeviceContextFilter` 注入 `DeviceContextHolder`，Controller 不要使用 `@RequestHeader` 读取设备号。
- OpenAPI Schema 命名遵循 `docs/specs/openapi.md` 中的 `*Request/*Response` 规则，确保与后端 DTO 保持一致。
- LinuxDo OAuth authorize 接口返回 `ApiResponse<LinuxDoAuthorizeResponse>`（包含 `state` 与 `authorizeUrl`），不再由后端直接发起 302 重定向。

如有新的后端约束或架构决策，请在此补充，方便团队统一执行。
