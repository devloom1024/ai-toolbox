-- ============================================
-- Auth 模块安全增强
-- 新增字段：登录失败次数、验证码失败锁定、Refresh Token 哈希
-- ============================================

-- t_user 表：添加登录失败次数和锁定时间
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS locked_at TIMESTAMPTZ;

COMMENT ON COLUMN t_user.failed_login_attempts IS '登录失败次数';
COMMENT ON COLUMN t_user.locked_at IS '账户锁定时间';

-- t_verification_code 表：添加验证失败次数和锁定时间
ALTER TABLE t_verification_code ADD COLUMN IF NOT EXISTS failed_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE t_verification_code ADD COLUMN IF NOT EXISTS locked_at TIMESTAMPTZ;

COMMENT ON COLUMN t_verification_code.failed_attempts IS '验证失败次数';
COMMENT ON COLUMN t_verification_code.locked_at IS '锁定时间';

-- t_refresh_token 表：添加 token_hash 字段
ALTER TABLE t_refresh_token ADD COLUMN IF NOT EXISTS token_hash VARCHAR(64) NOT NULL DEFAULT '';

COMMENT ON COLUMN t_refresh_token.token_hash IS 'Refresh Token 的 MD5 哈希值';

-- t_user 表：添加昵称唯一约束（可选，根据业务需求决定是否启用）
-- ALTER TABLE t_user ADD CONSTRAINT uk_t_user_nickname UNIQUE (nickname);
