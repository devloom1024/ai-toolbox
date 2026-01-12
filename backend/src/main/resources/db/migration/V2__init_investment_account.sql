-- 投资模块数据库初始化脚本
-- 创建投资账号表

-- 创建自动更新 updated_at 的触发器函数（如果不存在）
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============ 投资账号表 ============

CREATE TABLE t_account (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  account_type VARCHAR(32) NOT NULL 
    CHECK (account_type IN ('BROKER', 'FUND_PLATFORM', 'BANK', 'ALIPAY', 'OTHER')),
  account_name VARCHAR(64) NOT NULL,
  account_icon VARCHAR(512) NOT NULL DEFAULT '',
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
  sort_order SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_account_user_id ON t_account (user_id);

COMMENT ON TABLE t_account IS '投资账号';
COMMENT ON COLUMN t_account.id IS '账号主键';
COMMENT ON COLUMN t_account.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_account.account_type IS '账号类型：BROKER=券商 FUND_PLATFORM=基金平台 BANK=银行 ALIPAY=支付宝 OTHER=其他';
COMMENT ON COLUMN t_account.account_name IS '账号名称（用户自定义，如"东方财富A股"）';
COMMENT ON COLUMN t_account.account_icon IS '账号图标 URL';
COMMENT ON COLUMN t_account.is_active IS '是否启用';
COMMENT ON COLUMN t_account.is_hidden IS '是否隐藏（隐藏后不在列表显示）';
COMMENT ON COLUMN t_account.sort_order IS '排序序号';
COMMENT ON COLUMN t_account.created_at IS '创建时间';
COMMENT ON COLUMN t_account.updated_at IS '更新时间';

CREATE TRIGGER update_t_account_updated_at
BEFORE UPDATE ON t_account
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
