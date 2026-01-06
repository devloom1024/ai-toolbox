-- Create update_updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- Table: t_user
-- ============================================
CREATE TABLE t_user (
  id BIGSERIAL PRIMARY KEY,
  nickname VARCHAR(64) NOT NULL DEFAULT '',
  avatar VARCHAR(512) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_user IS '用户画像';
COMMENT ON COLUMN t_user.id IS '用户主键';
COMMENT ON COLUMN t_user.nickname IS '昵称';
COMMENT ON COLUMN t_user.avatar IS '头像 URL';
COMMENT ON COLUMN t_user.status IS '状态：ACTIVE=正常 LOCKED=锁定 DELETED=注销';
COMMENT ON COLUMN t_user.created_at IS '创建时间';
COMMENT ON COLUMN t_user.updated_at IS '更新时间';

CREATE TRIGGER update_t_user_updated_at
BEFORE UPDATE ON t_user
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Table: t_user_auth
-- ============================================
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

CREATE INDEX idx_t_user_auth_user_id ON t_user_auth (user_id);

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

CREATE TRIGGER update_t_user_auth_updated_at
BEFORE UPDATE ON t_user_auth
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Table: t_verification_code
-- ============================================
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

CREATE INDEX idx_t_verification_code_active 
  ON t_verification_code (identifier, channel, scene, expire_at) 
  WHERE used = FALSE;

COMMENT ON TABLE t_verification_code IS '验证码审计';
COMMENT ON COLUMN t_verification_code.id IS '记录主键';
COMMENT ON COLUMN t_verification_code.channel IS '渠道：EMAIL';
COMMENT ON COLUMN t_verification_code.scene IS '使用场景：REGISTER=注册 RESET_PASSWORD=重置密码';
COMMENT ON COLUMN t_verification_code.identifier IS '邮箱地址';
COMMENT ON COLUMN t_verification_code.code IS '验证码';
COMMENT ON COLUMN t_verification_code.expire_at IS '过期时间';
COMMENT ON COLUMN t_verification_code.used IS '是否已使用';
COMMENT ON COLUMN t_verification_code.created_at IS '创建时间';

-- ============================================
-- Table: t_refresh_token
-- ============================================
CREATE TABLE t_refresh_token (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL,
  device VARCHAR(64) NOT NULL DEFAULT '',
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_t_refresh_token_token UNIQUE (token)
);

CREATE INDEX idx_t_refresh_token_user_device 
  ON t_refresh_token (user_id, device, expires_at);

-- Ensure one token per user per device (service layer should prune expired rows)
CREATE UNIQUE INDEX uk_t_refresh_token_user_device
  ON t_refresh_token (user_id, device);

COMMENT ON TABLE t_refresh_token IS 'Refresh Token 管理';
COMMENT ON COLUMN t_refresh_token.id IS '记录主键';
COMMENT ON COLUMN t_refresh_token.user_id IS '用户 ID';
COMMENT ON COLUMN t_refresh_token.token IS 'Refresh Token 摘要';
COMMENT ON COLUMN t_refresh_token.device IS '终端标识';
COMMENT ON COLUMN t_refresh_token.expires_at IS '过期时间';
COMMENT ON COLUMN t_refresh_token.created_at IS '创建时间';

-- ============================================
-- Table: t_user_oauth_profile
-- ============================================
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

CREATE INDEX idx_t_user_oauth_user_id ON t_user_oauth_profile (user_id);

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

CREATE TRIGGER update_t_user_oauth_profile_updated_at
BEFORE UPDATE ON t_user_oauth_profile
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Table: t_login_audit
-- ============================================
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

CREATE INDEX idx_t_login_audit_user_created ON t_login_audit (user_id, created_at DESC);
CREATE INDEX idx_t_login_audit_created ON t_login_audit (created_at DESC);

COMMENT ON TABLE t_login_audit IS '登录审计';
COMMENT ON COLUMN t_login_audit.id IS '记录主键';
COMMENT ON COLUMN t_login_audit.user_id IS '用户 ID';
COMMENT ON COLUMN t_login_audit.identity_type IS '登录方式';
COMMENT ON COLUMN t_login_audit.identifier IS '登录标识（邮箱/LinuxDo 用户名等）';
COMMENT ON COLUMN t_login_audit.ip IS '登录 IP';
COMMENT ON COLUMN t_login_audit.user_agent IS 'User Agent';
COMMENT ON COLUMN t_login_audit.status IS '状态：1=成功 2=失败';
COMMENT ON COLUMN t_login_audit.created_at IS '登录时间';
