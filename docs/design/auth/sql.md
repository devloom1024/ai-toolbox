# 身份认证 SQL 设计

> 本文档基于 `docs/specs/postgresql.md` 的规范，整理可直接用于 Flyway 的 DDL 模板，所有字段均采用 `NOT NULL` 并指定默认值。

## 通用触发器函数

```sql
-- 创建自动更新 updated_at 的触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

## t_user

```sql
CREATE TABLE t_user (
  id BIGSERIAL PRIMARY KEY,
  nickname VARCHAR(64) NOT NULL DEFAULT '',
  avatar VARCHAR(512) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE t_user IS '用户画像';
COMMENT ON COLUMN t_user.id IS '用户主键';
COMMENT ON COLUMN t_user.nickname IS '昵称';
COMMENT ON COLUMN t_user.avatar IS '头像 URL';
COMMENT ON COLUMN t_user.status IS '状态：ACTIVE=正常 LOCKED=锁定 DELETED=注销';
COMMENT ON COLUMN t_user.created_at IS '创建时间';
COMMENT ON COLUMN t_user.updated_at IS '更新时间';

-- 创建自动更新触发器
CREATE TRIGGER update_t_user_updated_at
BEFORE UPDATE ON t_user
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
```

## t_user_auth

```sql
CREATE TABLE t_user_auth (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  identity_type VARCHAR(32) NOT NULL CHECK (identity_type IN ('EMAIL', 'LINUX_DO')),
  identifier VARCHAR(255) NOT NULL,
  credential VARCHAR(255) NOT NULL DEFAULT '',
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  last_login_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_t_user_auth_identity UNIQUE (identity_type, identifier)
);

-- 创建索引
CREATE INDEX idx_t_user_auth_user_id ON t_user_auth (user_id);

-- 添加注释
COMMENT ON TABLE t_user_auth IS '用户认证方式';
COMMENT ON COLUMN t_user_auth.id IS '记录主键';
COMMENT ON COLUMN t_user_auth.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_user_auth.identity_type IS '认证方式：EMAIL（当前支持）/LINUX_DO/PHONE（未来可能）';
COMMENT ON COLUMN t_user_auth.identifier IS '唯一标识：邮箱（当前）/手机号（未来可能）/LinuxDo 用户 ID';
COMMENT ON COLUMN t_user_auth.credential IS '凭证：BCrypt 密码或第三方令牌摘要';
COMMENT ON COLUMN t_user_auth.verified IS '是否已验证';
COMMENT ON COLUMN t_user_auth.last_login_at IS '最近登录时间';
COMMENT ON COLUMN t_user_auth.created_at IS '创建时间';
COMMENT ON COLUMN t_user_auth.updated_at IS '更新时间';

-- 创建自动更新触发器
CREATE TRIGGER update_t_user_auth_updated_at
BEFORE UPDATE ON t_user_auth
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
```

## t_verification_code

```sql
CREATE TABLE t_verification_code (
  id BIGSERIAL PRIMARY KEY,
  channel VARCHAR(16) NOT NULL CHECK (channel IN ('EMAIL')),
  scene VARCHAR(32) NOT NULL CHECK (scene IN ('REGISTER', 'RESET_PASSWORD')),
  identifier VARCHAR(255) NOT NULL,
  code VARCHAR(10) NOT NULL,
  expire_at TIMESTAMPTZ NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建部分索引（只索引未使用且未过期的验证码）
CREATE INDEX idx_t_verification_code_active 
  ON t_verification_code (identifier, channel, scene) 
  WHERE used = FALSE AND expire_at > CURRENT_TIMESTAMP;

-- 添加注释
COMMENT ON TABLE t_verification_code IS '验证码审计';
COMMENT ON COLUMN t_verification_code.id IS '记录主键';
COMMENT ON COLUMN t_verification_code.channel IS '渠道：EMAIL';
COMMENT ON COLUMN t_verification_code.scene IS '使用场景：REGISTER=注册 RESET_PASSWORD=重置密码';
COMMENT ON COLUMN t_verification_code.identifier IS '邮箱地址';
COMMENT ON COLUMN t_verification_code.code IS '验证码';
COMMENT ON COLUMN t_verification_code.expire_at IS '过期时间';
COMMENT ON COLUMN t_verification_code.used IS '是否已使用';
COMMENT ON COLUMN t_verification_code.created_at IS '创建时间';
```

## t_refresh_token

```sql
CREATE TABLE t_refresh_token (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token CHAR(64) NOT NULL,
  device VARCHAR(64) NOT NULL DEFAULT '',
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_t_refresh_token_token UNIQUE (token)
);

-- 创建部分索引（只索引未过期的 Token）
CREATE INDEX idx_t_refresh_token_user_device 
  ON t_refresh_token (user_id, device) 
  WHERE expires_at > CURRENT_TIMESTAMP;

-- 确保同一用户在同一设备上只有一个有效的 Refresh Token
CREATE UNIQUE INDEX uk_t_refresh_token_user_device
  ON t_refresh_token (user_id, device)
  WHERE expires_at > CURRENT_TIMESTAMP;

-- 添加注释
COMMENT ON TABLE t_refresh_token IS 'Refresh Token 管理';
COMMENT ON COLUMN t_refresh_token.id IS '记录主键';
COMMENT ON COLUMN t_refresh_token.user_id IS '用户 ID';
COMMENT ON COLUMN t_refresh_token.token IS 'Refresh Token 摘要';
COMMENT ON COLUMN t_refresh_token.device IS '终端标识';
COMMENT ON COLUMN t_refresh_token.expires_at IS '过期时间';
COMMENT ON COLUMN t_refresh_token.created_at IS '创建时间';
```

## t_user_oauth_profile

```sql
CREATE TABLE t_user_oauth_profile (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  identity_type VARCHAR(32) NOT NULL CHECK (identity_type = 'LINUX_DO'),
  oauth_user_id VARCHAR(255) NOT NULL,
  oauth_username VARCHAR(255) NOT NULL DEFAULT '',
  oauth_email VARCHAR(255) NOT NULL DEFAULT '',
  oauth_avatar VARCHAR(512) NOT NULL DEFAULT '',
  access_token TEXT NOT NULL DEFAULT '',
  refresh_token TEXT NOT NULL DEFAULT '',
  token_expires_at TIMESTAMPTZ,
  raw_profile JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_t_user_oauth_identity UNIQUE (identity_type, oauth_user_id)
);

-- 创建索引
CREATE INDEX idx_t_user_oauth_user_id ON t_user_oauth_profile (user_id);

-- 添加注释
COMMENT ON TABLE t_user_oauth_profile IS 'OAuth 第三方登录资料';
COMMENT ON COLUMN t_user_oauth_profile.id IS '记录主键';
COMMENT ON COLUMN t_user_oauth_profile.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_user_oauth_profile.identity_type IS '第三方平台：LINUX_DO';
COMMENT ON COLUMN t_user_oauth_profile.oauth_user_id IS 'LinuxDo 用户 ID';
COMMENT ON COLUMN t_user_oauth_profile.oauth_username IS 'LinuxDo 用户名';
COMMENT ON COLUMN t_user_oauth_profile.oauth_email IS 'LinuxDo 邮箱';
COMMENT ON COLUMN t_user_oauth_profile.oauth_avatar IS 'LinuxDo 头像 URL';
COMMENT ON COLUMN t_user_oauth_profile.access_token IS 'OAuth Access Token（建议加密存储）';
COMMENT ON COLUMN t_user_oauth_profile.refresh_token IS 'OAuth Refresh Token（建议加密存储）';
COMMENT ON COLUMN t_user_oauth_profile.token_expires_at IS 'Access Token 过期时间';
COMMENT ON COLUMN t_user_oauth_profile.raw_profile IS 'LinuxDo 返回的完整用户信息（JSON）';
COMMENT ON COLUMN t_user_oauth_profile.created_at IS '创建时间';
COMMENT ON COLUMN t_user_oauth_profile.updated_at IS '更新时间';

-- 创建自动更新触发器
CREATE TRIGGER update_t_user_oauth_profile_updated_at
BEFORE UPDATE ON t_user_oauth_profile
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
```

## t_login_audit

```sql
CREATE TABLE t_login_audit (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  identity_type VARCHAR(32) NOT NULL,
  identifier VARCHAR(255) NOT NULL,
  ip INET,
  user_agent VARCHAR(255) NOT NULL DEFAULT '',
  status SMALLINT NOT NULL DEFAULT 1 CHECK (status IN (1, 2)),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_t_login_audit_user_created ON t_login_audit (user_id, created_at DESC);
CREATE INDEX idx_t_login_audit_created ON t_login_audit (created_at DESC);

-- 添加注释
COMMENT ON TABLE t_login_audit IS '登录审计';
COMMENT ON COLUMN t_login_audit.id IS '记录主键';
COMMENT ON COLUMN t_login_audit.user_id IS '用户 ID';
COMMENT ON COLUMN t_login_audit.identity_type IS '登录方式';
COMMENT ON COLUMN t_login_audit.identifier IS '登录标识（邮箱/LinuxDo 用户名等）';
COMMENT ON COLUMN t_login_audit.ip IS '登录 IP';
COMMENT ON COLUMN t_login_audit.user_agent IS 'User Agent';
COMMENT ON COLUMN t_login_audit.status IS '状态：1=成功 2=失败';
COMMENT ON COLUMN t_login_audit.created_at IS '登录时间';
```

---

## 设计说明

### 核心特性

- **自动更新时间戳**：使用触发器实现 `updated_at` 字段自动更新
- **部分索引**：`t_verification_code` 和 `t_refresh_token` 使用部分索引，只索引有效数据
- **CHECK 约束**：限制枚举值的有效范围
- **INET 类型**：IP 地址使用原生类型，支持网络运算和查询
- **BOOLEAN 类型**：布尔字段使用原生类型，语义更明确

### 设计决策

- ✅ **不使用外键约束**：应用层控制关联关系，便于后续分库分表和微服务拆分
- ✅ **TIMESTAMPTZ**：所有时间字段带时区，避免时区问题
- ✅ **枚举值策略**：
  - `t_user.status` 使用 VARCHAR + CHECK（可读性优先）
  - `t_login_audit.status` 使用 SMALLINT + CHECK（性能优先，数据量大）
- ✅ **索引优化**：只为活跃数据创建索引，节省空间和提升性能

---

## 业务流程说明

### 邮箱注册流程
1. 前端调用 `/api/v1/auth/code/email`（scene=REGISTER）获取验证码
2. 后端插入 `t_verification_code` 记录并发送邮件
3. 前端提交注册信息到 `/api/v1/auth/register`
4. 后端在**单个事务**中：
   - 校验验证码（查询 `t_verification_code`，验证 scene=REGISTER）
   - 创建用户（插入 `t_user`）
   - 创建认证信息（插入 `t_user_auth`，identity_type=EMAIL，verified=TRUE）
   - 标记验证码已使用（更新 `t_verification_code.used=TRUE`）
   - 创建 Refresh Token（插入 `t_refresh_token`）

### LinuxDo OAuth 登录流程
1. 前端跳转到 `/api/v1/auth/oauth/linuxdo/authorize`
2. 后端生成 state 并重定向到 LinuxDo OAuth 授权页面
3. 用户在 LinuxDo 授权后，LinuxDo 回调 `/api/v1/auth/oauth/linuxdo/callback`
4. 后端在**单个事务**中：
   - 验证 state 参数
   - 用 OAuth code 换取 access_token
   - 调用 LinuxDo API 获取用户信息
   - 查询 `t_user_oauth_profile` 检查是否已注册
   - **如果未注册**：
     - 创建 `t_user`（nickname 使用 LinuxDo 用户名，avatar 使用 LinuxDo 头像）
     - 创建 `t_user_auth`（identity_type=LINUX_DO，identifier=LinuxDo用户ID，verified=TRUE）
     - 创建 `t_user_oauth_profile`（存储 OAuth token 和用户资料）
   - **如果已注册**：
     - 更新 `t_user_oauth_profile` 的 token 和资料
     - 更新 `t_user_auth.last_login_at`
   - 创建 Refresh Token（插入 `t_refresh_token`）

### 重置密码流程
1. 前端调用 `/api/v1/auth/code/email`（scene=RESET_PASSWORD）获取验证码
2. 后端插入 `t_verification_code` 记录并发送邮件
3. 前端提交重置密码信息到 `/api/v1/auth/password/reset`
4. 后端在**单个事务**中：
   - 校验验证码（查询 `t_verification_code`，验证 scene=RESET_PASSWORD）
   - 查询 `t_user_auth` 确认用户存在
   - 更新密码（更新 `t_user_auth.credential`）
   - 标记验证码已使用（更新 `t_verification_code.used=TRUE`）
   - 删除该用户的所有 Refresh Token（安全考虑，强制重新登录）
