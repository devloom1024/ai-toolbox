# 后端 Agent 规范

- Controller 必须返回 `ApiResponse<T>`，禁止直接返回 `ResponseEntity` 或裸 DTO，必要时请新增 DTO 或调整服务层。
- 设备标识（如 `X-Device-Id`）统一由 `DeviceContextFilter` 注入 `DeviceContextHolder`，Controller 不要使用 `@RequestHeader` 读取设备号。
- OpenAPI Schema 命名遵循 `docs/specs/openapi.md` 中的 `*Request/*Response` 规则，确保与后端 DTO 保持一致。
- LinuxDo OAuth authorize 接口返回 `ApiResponse<LinuxDoAuthorizeResponse>`（包含 `state` 与 `authorizeUrl`），不再由后端直接发起 302 重定向。
- Controller 的每个方法必须编写注释说明用途；所有入参 DTO 与返回 DTO 的字段都要有注释。
- 仓储层的 Entity 字段必须写明含义，与 `db/migration` 中的表结构保持一致。
- 所有枚举类型需要补充注释，说明各枚举值的语义。
- Bean Validation 注解（如 `@NotBlank(message = "...")`）的 `message` 不得为空，且必须引用国际化资源键。
- 所有通过接口返回的 message（包括异常、业务提示等）必须支持国际化，统一从 `messages/*.properties` 读取。

如有新的后端约束或架构决策，请在此补充，方便团队统一执行。
