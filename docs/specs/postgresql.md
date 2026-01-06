# PostgreSQL 规范

> 本规范参考 PostgreSQL 官方文档、PostgreSQL Wiki 以及社区最佳实践，总结 PostgreSQL 在设计、开发、运维阶段的核心要求。适用于使用 PostgreSQL 作为主数据库的 Java 应用开发。

## 命名规范

### 基础命名
- **表名、字段名**：全部小写，使用下划线分隔，禁止使用复数；如 `t_user_auth`。
- **表前缀**：统一为业务实体增加前缀 `t_` 或 `rel_`（关系表）等标识，避免与系统关键字冲突。
- **索引命名**：
  - B-Tree 索引：`idx_表名_字段名`
  - 唯一索引：`uk_表名_字段名`
  - GIN/GIST 索引：`gin_表名_字段名` / `gist_表名_字段名`
- **约束命名**：
  - 主键：`pk_表名`（通常自动生成）
  - 外键：`fk_表名_引用表名`
  - 检查约束：`ck_表名_字段名`
- **序列命名**：表名和字段名组合，如 `t_user_id_seq`（SERIAL 自动创建）。
- **临时表**：以 `tmp_` 开头并在会话结束或当天清理；备份表以 `bak_` 开头并注明日期。

### Schema 规范
- 默认使用 `public` schema，生产环境建议为不同业务模块创建独立 schema（如 `auth`、`order`）。
- Schema 命名使用单数小写名词，如 `auth`、`analytics`。
- 避免使用 `pg_` 开头的名称（系统保留）。

---

## 表设计规范

### 主键设计
- **每张表必须有主键**，推荐使用以下三种方式之一：
  1. **BIGSERIAL**：适合中小规模单库场景
     ```sql
     id BIGSERIAL PRIMARY KEY
     ```
  2. **UUID**：分布式场景或需要全局唯一性
     ```sql
     id UUID PRIMARY KEY DEFAULT gen_random_uuid()
     ```
  3. **雪花 ID**：高并发分布式系统（需应用层生成）
     ```sql
     id BIGINT PRIMARY KEY
     ```
- 禁止以业务字段（如邮箱、手机号）作为主键。

### 字段规范
- **避免单表列数超过 50**，确保每张表字段职责单一。
- **大字段独立拆表**：TEXT、BYTEA、JSONB（超 1KB）等大字段建议独立存储，通过关联字段查询。
- **时间戳字段必备**：每张表必须包含以下字段：
  ```sql
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
  ```
  - 使用 `TIMESTAMPTZ`（带时区）而非 `TIMESTAMP`，避免时区问题。
  - `updated_at` 需配合触发器自动更新（见下文）。

### NOT NULL 与默认值
- **所有字段必须设置 NOT NULL**，并提供合理默认值；确需允许为空的场景需在设计文档中说明。
- 字符串默认值使用 `''` 而非 `NULL`。
- 数值类型默认值根据业务含义设置（如状态字段默认 `1`）。

### 注释规范
- **所有表和字段必须添加注释（COMMENT）**，明确业务含义。
- 注释使用独立语句编写：
  ```sql
  COMMENT ON TABLE t_user IS '用户画像';
  COMMENT ON COLUMN t_user.id IS '用户主键';
  COMMENT ON COLUMN t_user.status IS '1=正常 2=锁定 3=注销';
  ```

---

## 数据类型规范

### 数值类型
| 场景 | 推荐类型 | 说明 |
|------|---------|------|
| 自增主键 | `BIGSERIAL` | 等价于 `BIGINT` + 序列 |
| 整数（小范围） | `SMALLINT` | -32768 ~ 32767 |
| 整数（常规） | `INTEGER` | -2^31 ~ 2^31-1 |
| 整数（大数） | `BIGINT` | -2^63 ~ 2^63-1 |
| 货币/金额 | `NUMERIC(18,4)` | 精确计算，避免浮点误差 |
| 百分比/比率 | `NUMERIC(5,4)` | 如 0.9876 表示 98.76% |
| 计数器 | `BIGINT` | 避免溢出 |

- **禁止使用 REAL、DOUBLE PRECISION 存储货币**。
- PostgreSQL 无 `UNSIGNED` 类型，需正数约束时使用 `CHECK`：
  ```sql
  age SMALLINT CHECK (age >= 0)
  ```

### 字符串类型
- **变长字符串**：优先使用 `VARCHAR(n)`，避免使用无限制的 `TEXT`（除非真的需要大文本）。
- **定长字符串**：仅在确定长度场景使用 `CHAR(n)`（如 MD5 哈希 `CHAR(32)`）。
- **文本类型**：
  - 短文本（< 255）：`VARCHAR(255)`
  - 中等文本（255 ~ 5000）：`VARCHAR(5000)` 或 `TEXT`
  - 大文本（> 5000）：`TEXT`，并考虑独立表存储
- **字符集**：PostgreSQL 默认 UTF8 编码，支持完整 Unicode 字符集。

### 时间类型
| 场景 | 推荐类型 | 说明 |
|------|---------|------|
| 带时区时间戳 | `TIMESTAMPTZ` | **强烈推荐**，自动处理时区转换 |
| 不带时区时间戳 | `TIMESTAMP` | 仅在明确不需要时区时使用 |
| 日期 | `DATE` | 仅存储年月日 |
| 时间 | `TIME` / `TIMETZ` | 仅存储时分秒 |
| 时间间隔 | `INTERVAL` | 如 '2 hours 30 minutes' |

- **默认使用 TIMESTAMPTZ**，避免未来时区问题。
- 禁止使用 `BIGINT` 存储时间戳毫秒数（除非与外部系统对接需要）。

### 布尔类型
- 使用 `BOOLEAN` 类型，而非 `SMALLINT` 模拟。
- 默认值明确设置：
  ```sql
  is_active BOOLEAN NOT NULL DEFAULT TRUE
  ```

### 枚举类型

PostgreSQL 中存储枚举有三种常见方案，根据场景选择：

#### 方案一：SMALLINT + 常量映射（性能优先）
- **适用场景**：高并发、大数据量、枚举值稳定
- **优势**：
  - ✅ 存储最紧凑（2 字节）
  - ✅ 索引和查询性能最优
  - ✅ 与其他数据库（MySQL）迁移兼容
- **劣势**：
  - ❌ 数据库中不直观，需查映射表
  - ❌ SQL 查询结果需转换才能理解
  
```sql
-- 表定义
status SMALLINT NOT NULL DEFAULT 1 CHECK (status BETWEEN 1 AND 3),

-- 注释说明映射关系
COMMENT ON COLUMN t_user.status IS '状态：1=正常 2=锁定 3=注销';

-- 代码层枚举常量
public enum UserStatus {
    ACTIVE(1, "正常"),
    LOCKED(2, "锁定"),
    DELETED(3, "注销");
    
    private final int code;
    private final String desc;
}
```

#### 方案二：VARCHAR + CHECK 约束（可读性优先）
- **适用场景**：中小规模、可读性要求高、需经常手动查询
- **优势**：
  - ✅ 数据库中直接可读
  - ✅ 无需维护映射表
  - ✅ 代码更清晰，调试更方便
- **劣势**：
  - ❌ 存储空间较大（通常 5-20 字节）
  - ❌ 索引和比较性能略差

```sql
-- 表定义
status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' 
  CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED')),

COMMENT ON COLUMN t_user.status IS '状态：ACTIVE=正常 LOCKED=锁定 DELETED=注销';

-- SQL 查询直观可读
SELECT id, nickname, status FROM t_user WHERE status = 'ACTIVE';
```

#### 方案三：ENUM 类型（不推荐）
- **不推荐原因**：
  - ❌ 修改枚举值需要 ALTER TYPE，无法在事务中安全执行
  - ❌ Flyway 迁移复杂，容易出错
  - ❌ 跨环境同步麻烦
- **仅适用于**：枚举值永不变化的系统常量（如星期、月份）

#### 选择建议
- **默认推荐**：**VARCHAR + CHECK**（可读性好，维护成本低）
- **性能敏感**：**SMALLINT**（大表、高并发场景）
- **特殊场景**：避免使用 ENUM（除非枚举值绝对不变）

### JSON 类型
- **优先使用 JSONB**（二进制存储，支持索引），而非 JSON：
  ```sql
  preferences JSONB NOT NULL DEFAULT '{}'::jsonb
  ```
- 适用场景：用户配置、扩展属性、灵活的元数据。
- 配合 GIN 索引使用：
  ```sql
  CREATE INDEX idx_t_user_preferences ON t_user USING GIN (preferences);
  ```

### 数组类型
- PostgreSQL 支持原生数组，适用于标签、权限等场景：
  ```sql
  tags TEXT[] NOT NULL DEFAULT '{}'
  roles VARCHAR(32)[] NOT NULL DEFAULT ARRAY[]::VARCHAR[]
  ```
- 配合 GIN 索引支持快速查询：
  ```sql
  CREATE INDEX idx_t_user_tags ON t_user USING GIN (tags);
  -- 查询示例：SELECT * FROM t_user WHERE tags @> ARRAY['admin'];
  ```

### 网络类型
- **IP 地址**：使用 `INET` 或 `CIDR`，而非 VARCHAR：
  ```sql
  ip_address INET NOT NULL
  ```
- 支持网络运算和索引。

### UUID 类型
- 使用 `UUID` 类型存储全局唯一标识：
  ```sql
  trace_id UUID NOT NULL DEFAULT gen_random_uuid()
  ```
- 需要 `gen_random_uuid()` 函数（PostgreSQL 13+ 默认可用）。

---

## 索引与性能

### 索引设计原则
- **依据最左前缀原则创建联合索引**；WHERE、ORDER BY、GROUP BY 中出现的列必须有索引。
- **控制单张表索引数量**：建议不超过 6 个，联合索引字段数不超过 5。
- **索引名称包含表名**，避免全局冲突：`idx_t_user_email` 而非 `idx_email`。

### B-Tree 索引（默认）
- 适用于等值查询、范围查询、排序：
  ```sql
  CREATE INDEX idx_t_user_email ON t_user (email);
  CREATE INDEX idx_t_user_created_at ON t_user (created_at DESC);
  ```

### 唯一索引
- 确保业务唯一性：
  ```sql
  CREATE UNIQUE INDEX uk_t_user_auth_identity ON t_user_auth (identity_type, identifier);
  ```

### 部分索引（Partial Index）
- **PostgreSQL 特色优化**，仅索引满足条件的行：
  ```sql
  -- 只索引活跃用户
  CREATE INDEX idx_t_user_active ON t_user (id) WHERE status = 'ACTIVE';
  
  -- 只索引未使用的验证码
  CREATE INDEX idx_verification_unused 
    ON t_verification_code (identifier, channel) 
    WHERE used = FALSE AND expire_at > CURRENT_TIMESTAMP;
  ```
- 优势：节省空间，提升查询速度。

### 表达式索引
- 对函数或表达式结果建立索引：
  ```sql
  -- 不区分大小写的邮箱查询
  CREATE INDEX idx_t_user_email_lower ON t_user (LOWER(email));
  
  -- 查询时必须使用相同表达式
  SELECT * FROM t_user WHERE LOWER(email) = 'user@example.com';
  ```

### GIN 索引（通用倒排索引）
- 适用于 JSONB、数组、全文检索：
  ```sql
  -- JSONB 索引
  CREATE INDEX idx_t_user_preferences ON t_user USING GIN (preferences);
  
  -- 数组索引
  CREATE INDEX idx_t_user_tags ON t_user USING GIN (tags);
  
  -- 全文检索（见下文）
  CREATE INDEX idx_t_article_fts ON t_article USING GIN (to_tsvector('simple', content));
  ```

### BRIN 索引（块范围索引）
- 适用于大表且数据有序（如时间序列）：
  ```sql
  CREATE INDEX idx_t_login_audit_created_at ON t_login_audit USING BRIN (created_at);
  ```
- 优势：索引体积极小，适合 TB 级别数据。

### 覆盖索引（Index-Only Scan）
- 使用 `INCLUDE` 将额外列加入索引（PostgreSQL 11+）：
  ```sql
  CREATE INDEX idx_t_user_email_incl 
    ON t_user (email) INCLUDE (nickname, avatar);
  ```
- 查询可直接从索引返回，无需回表。

---

## 约束与完整性

### 主键约束
- 自动创建唯一索引，无需额外操作。

### 外键约束
- **推荐方案：应用层控制关联关系**
  - ✅ 性能更优：避免写入时的外键检查开销
  - ✅ 灵活性高：便于分库分表、微服务拆分
  - ✅ 可控性强：业务逻辑集中在代码层，易于维护和测试
  
- **外键使用场景**（可选）：
  - 仅适用于中小规模单库应用
  - 数据一致性要求极高且性能要求不高的场景
  - 如需使用，建议配置级联策略：
    ```sql
    CONSTRAINT fk_user_auth_user FOREIGN KEY (user_id) 
      REFERENCES t_user (id) ON DELETE CASCADE
    ```
  
- **级联策略说明**：
  - `ON DELETE CASCADE`：删除父记录时自动删除子记录
  - `ON DELETE SET NULL`：删除父记录时子记录外键置空
  - `ON DELETE RESTRICT`：有子记录时禁止删除父记录（默认）

- **权衡建议**：对于高并发、需要水平扩展的系统，建议在应用层通过事务控制数据一致性，避免使用外键。

### 唯一约束
- 确保业务唯一性：
  ```sql
  CONSTRAINT uk_identity UNIQUE (identity_type, identifier)
  ```

### 检查约束
- 确保字段值合法性：
  ```sql
  CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED'))
  CHECK (age >= 0 AND age <= 150)
  CHECK (start_date <= end_date)
  ```

### NOT NULL 约束
- 所有字段默认设置，除非业务必须允许空值。

---

## 触发器与函数

### 自动更新 updated_at
- 创建通用触发器函数：
  ```sql
  CREATE OR REPLACE FUNCTION update_updated_at_column()
  RETURNS TRIGGER AS $$
  BEGIN
     NEW.updated_at = CURRENT_TIMESTAMP;
     RETURN NEW;
  END;
  $$ LANGUAGE plpgsql;
  ```

- 为每张表创建触发器：
  ```sql
  CREATE TRIGGER update_t_user_updated_at
  BEFORE UPDATE ON t_user
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();
  ```

### 软删除触发器
- 记录删除日志：
  ```sql
  CREATE OR REPLACE FUNCTION log_deletion()
  RETURNS TRIGGER AS $$
  BEGIN
     INSERT INTO t_deletion_log (table_name, record_id, deleted_at)
     VALUES (TG_TABLE_NAME, OLD.id, CURRENT_TIMESTAMP);
     RETURN OLD;
  END;
  $$ LANGUAGE plpgsql;
  ```

---

## SQL 使用规范

### 查询规范
- **禁止 SELECT ***，必须显式列出字段：
  ```sql
  -- ❌ 错误
  SELECT * FROM t_user;
  
  -- ✅ 正确
  SELECT id, nickname, email FROM t_user;
  ```

- **分页查询必须带 ORDER BY**，并推荐使用 Keyset Pagination：
  ```sql
  -- ❌ 深度分页性能差
  SELECT * FROM t_user ORDER BY id LIMIT 1000 OFFSET 100000;
  
  -- ✅ Keyset Pagination
  SELECT * FROM t_user WHERE id > 100000 ORDER BY id LIMIT 1000;
  ```

- **IN 条件元素数控制在 1000 以内**，超出使用 ANY：
  ```sql
  SELECT * FROM t_user WHERE id = ANY(ARRAY[1,2,3,...]);
  ```

### 写入规范
- **批量插入使用单条语句**：
  ```sql
  INSERT INTO t_user (nickname, email) VALUES
    ('User1', 'user1@example.com'),
    ('User2', 'user2@example.com');
  ```
  
- **大批量写入（超 1000 条）使用 COPY**：
  ```sql
  COPY t_user (nickname, email) FROM '/path/to/data.csv' WITH CSV;
  ```

- **ON CONFLICT 实现 UPSERT**：
  ```sql
  INSERT INTO t_user_auth (user_id, identity_type, identifier, credential)
  VALUES (1, 'EMAIL', 'user@example.com', 'hashed_password')
  ON CONFLICT (identity_type, identifier) 
  DO UPDATE SET credential = EXCLUDED.credential, updated_at = CURRENT_TIMESTAMP;
  ```

### 事务规范
- **显式使用事务**，避免自动提交：
  ```sql
  BEGIN;
  -- 业务逻辑
  COMMIT;
  ```

- **设置合理的隔离级别**：
  - `READ COMMITTED`（默认）：适合大多数场景
  - `REPEATABLE READ`：需要一致性快照
  - `SERIALIZABLE`：最高隔离级别，避免幻读

- **避免长事务**，控制事务时长在秒级。

### 安全规范
- **WHERE 条件必须使用参数化**，防止 SQL 注入：
  ```java
  // ✅ 正确
  jdbcTemplate.query("SELECT * FROM t_user WHERE email = ?", email);
  
  // ❌ 错误
  jdbcTemplate.query("SELECT * FROM t_user WHERE email = '" + email + "'");
  ```

- **禁止在数据库端做过度计算**，复杂逻辑放在业务层。

---

## 高级特性

### 全文检索
- 使用 `tsvector` 和 `tsquery`：
  ```sql
  -- 添加全文检索列
  ALTER TABLE t_article ADD COLUMN content_tsvector TSVECTOR;
  
  -- 创建 GIN 索引
  CREATE INDEX idx_t_article_fts ON t_article USING GIN (content_tsvector);
  
  -- 自动更新触发器
  CREATE TRIGGER tsvector_update_t_article
  BEFORE INSERT OR UPDATE ON t_article
  FOR EACH ROW
  EXECUTE FUNCTION tsvector_update_trigger(
    content_tsvector, 'pg_catalog.simple', content
  );
  
  -- 全文搜索查询
  SELECT * FROM t_article 
  WHERE content_tsvector @@ to_tsquery('simple', 'PostgreSQL & database');
  ```

### 分区表
- 适用于大表（超 1 亿行或 100GB）：
  ```sql
  -- 创建分区主表（按月分区）
  CREATE TABLE t_login_audit (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id, created_at)
  ) PARTITION BY RANGE (created_at);
  
  -- 创建子分区
  CREATE TABLE t_login_audit_2026_01 PARTITION OF t_login_audit
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
  
  CREATE TABLE t_login_audit_2026_02 PARTITION OF t_login_audit
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
  ```

### 物化视图
- 缓存复杂查询结果：
  ```sql
  CREATE MATERIALIZED VIEW mv_user_stats AS
  SELECT 
    u.id, 
    u.nickname,
    COUNT(la.id) AS login_count
  FROM t_user u
  LEFT JOIN t_login_audit la ON u.id = la.user_id
  GROUP BY u.id, u.nickname;
  
  -- 创建索引
  CREATE INDEX idx_mv_user_stats_id ON mv_user_stats (id);
  
  -- 定期刷新
  REFRESH MATERIALIZED VIEW CONCURRENTLY mv_user_stats;
  ```

### 行级安全（RLS）
- 实现多租户数据隔离：
  ```sql
  ALTER TABLE t_user ENABLE ROW LEVEL SECURITY;
  
  CREATE POLICY user_isolation ON t_user
    USING (tenant_id = current_setting('app.current_tenant')::BIGINT);
  ```

---

## 性能优化

### EXPLAIN ANALYZE
- 所有慢查询必须通过 EXPLAIN ANALYZE 分析：
  ```sql
  EXPLAIN (ANALYZE, BUFFERS, VERBOSE) 
  SELECT * FROM t_user WHERE email = 'user@example.com';
  ```

### 连接池配置
- 推荐使用 HikariCP，配置示例：
  ```properties
  spring.datasource.hikari.maximum-pool-size=20
  spring.datasource.hikari.minimum-idle=5
  spring.datasource.hikari.connection-timeout=30000
  spring.datasource.hikari.idle-timeout=600000
  spring.datasource.hikari.max-lifetime=1800000
  ```

### VACUUM 与统计信息
- 定期 VACUUM 回收空间：
  ```sql
  VACUUM ANALYZE t_user;
  ```
- 生产环境启用 autovacuum（默认开启）。

### 慢查询日志
- 配置 `postgresql.conf`：
  ```ini
  log_min_duration_statement = 1000  # 记录超过 1 秒的查询
  log_line_prefix = '%t [%p]: [%l-1] user=%u,db=%d,app=%a,client=%h '
  ```

---

## 运维与安全

### 字符集与排序规则
- 数据库创建时指定：
  ```sql
  CREATE DATABASE ai_toolbox
    ENCODING = 'UTF8'
    LC_COLLATE = 'zh_CN.UTF-8'
    LC_CTYPE = 'zh_CN.UTF-8'
    TEMPLATE = template0;
  ```

### 版本化 DDL
- **所有结构变更通过 Flyway 执行**，禁止手工改表：
  ```sql
  -- V1__init_schema.sql
  -- V2__add_user_preferences.sql
  ```

### 备份策略
- **逻辑备份**（小于 100GB）：
  ```bash
  pg_dump -h localhost -U postgres -d ai_toolbox -F c -f backup.dump
  ```

- **物理备份**（大于 100GB）：
  ```bash
  pg_basebackup -h localhost -U postgres -D /backup -Fp -Xs -P
  ```

- **增量备份**：使用 WAL 归档 + PITR。

### 权限管理
- 遵循最小权限原则：
  ```sql
  -- 创建应用用户
  CREATE USER app_user WITH PASSWORD 'secure_password';
  
  -- 仅授予必要权限
  GRANT CONNECT ON DATABASE ai_toolbox TO app_user;
  GRANT USAGE ON SCHEMA public TO app_user;
  GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
  GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;
  ```

### 连接安全
- 使用 SSL 连接：
  ```properties
  spring.datasource.url=jdbc:postgresql://localhost:5432/ai_toolbox?sslmode=require
  ```

- 配置 `pg_hba.conf` 限制访问：
  ```
  # TYPE  DATABASE        USER            ADDRESS                 METHOD
  host    ai_toolbox      app_user        10.0.0.0/8              md5
  host    ai_toolbox      all             127.0.0.1/32            trust
  ```

### 监控指标
- 必须监控的指标：
  - 连接数：`SELECT count(*) FROM pg_stat_activity;`
  - 慢查询：检查 `pg_stat_statements`
  - 数据库大小：`SELECT pg_size_pretty(pg_database_size('ai_toolbox'));`
  - 表膨胀率：监控 VACUUM 效果
  - 缓存命中率：`SELECT sum(blks_hit) / sum(blks_read + blks_hit) FROM pg_stat_database;`

---

## 总结

PostgreSQL 核心优势：
1. ✅ **更丰富的数据类型**：JSONB、数组、INET、UUID、BOOLEAN 等
2. ✅ **更强大的索引**：部分索引、表达式索引、GIN、BRIN、覆盖索引等
3. ✅ **更完善的事务**：MVCC 实现优雅，支持 SERIALIZABLE 隔离级别
4. ✅ **更多高级特性**：全文检索、分区表、物化视图、行级安全
5. ✅ **更严格的标准遵循**：SQL 标准兼容性好

最佳实践要点：
- ⚡ 使用 TIMESTAMPTZ 而非 TIMESTAMP
- ⚡ 使用 BOOLEAN 而非整型模拟
- ⚡ 避免使用 ENUM，用 VARCHAR + CHECK
- ⚡ 推荐应用层控制外键关系
- ⚡ 充分利用 JSONB、数组等高级类型
- ⚡ 善用部分索引和表达式索引优化性能
- ⚡ 大表使用分区表设计

> 若业务场景有特殊需求，可在评审通过后另行补充，但不得违反以上底线要求。

