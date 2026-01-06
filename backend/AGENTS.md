# 后端 Agent 规范

- Controller 必须返回 `ApiResponse<T>`，禁止直接返回 `ResponseEntity` 或裸 DTO，必要时请新增 DTO 或调整服务层。
- 设备标识（如 `X-Device-Id`）统一由 `DeviceContextFilter` 注入 `DeviceContextHolder`，Controller 不要使用 `@RequestHeader` 读取设备号。
- OpenAPI Schema 命名遵循 `docs/specs/openapi.md` 中的 `*Request/*Response` 规则，确保与后端 DTO 保持一致。
- LinuxDo OAuth authorize 接口返回 `ApiResponse<LinuxDoAuthorizeResponse>`（包含 `state` 与 `authorizeUrl`），不再由后端直接发起 302 重定向。
- Controller 的每个方法必须编写注释说明用途；所有入参 DTO 与返回 DTO 的字段都要有注释。
- 仓储层的 Entity 字段必须写明含义，与 `db/migration` 中的表结构保持一致。
- 当 DDL 使用 PostgreSQL 特有类型（如 `INET`、JSONB、SMALLINT 枚举等）时，Entity 需要添加 `@JdbcTypeCode`/`columnDefinition` 或 `AttributeConverter` 来保持类型一致；新增/修改字段后务必对照 Flyway 脚本进行 schema 校验再提交。
- 后端的环境变量只放在 `backend/.env`，Spring Boot (`spring.config.import`) 与 Docker Compose 共用该文件；如需调整变量名或默认值，请同步更新 `.env.example` 并在 PR 中说明。
- 所有枚举类型需要补充注释，说明各枚举值的语义。
- Bean Validation 注解（如 `@NotBlank(message = "...")`）的 `message` 不得为空，且必须引用国际化资源键。
- 所有通过接口返回的 message（包括异常、业务提示等）必须支持国际化，统一从 `messages/*.properties` 读取。
- Service 层使用 `@Transactional` 时必须显式声明 `rollbackFor = {Exception.class, Error.class}`，避免受检异常或 `Error` 无法触发回滚；
- 严禁在同一个 Spring Bean 内通过 `this` 或直接调用方式触发另一个带 `@Transactional` 的方法，否则事务代理不会生效；如需复用逻辑，请通过注入自身代理或抽取到独立组件。

如有新的后端约束或架构决策，请在此补充，方便团队统一执行。
