-- 投资模块数据库迁移脚本
-- 创建自选股和自选分组表

-- ============ 自选股分组表 ============

CREATE TABLE t_watchlist_group (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  sort_order SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_watchlist_group_user_id ON t_watchlist_group (user_id);

COMMENT ON TABLE t_watchlist_group IS '自选股分组';
COMMENT ON COLUMN t_watchlist_group.id IS '分组主键';
COMMENT ON COLUMN t_watchlist_group.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_watchlist_group.name IS '分组名称';
COMMENT ON COLUMN t_watchlist_group.sort_order IS '排序序号';
COMMENT ON COLUMN t_watchlist_group.created_at IS '创建时间';
COMMENT ON COLUMN t_watchlist_group.updated_at IS '更新时间';

CREATE TRIGGER update_t_watchlist_group_updated_at
BEFORE UPDATE ON t_watchlist_group
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============ 自选股表 ============

CREATE TABLE t_watchlist (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  group_id BIGINT,
  symbol VARCHAR(32) NOT NULL,
  market VARCHAR(16) NOT NULL
    CHECK (market IN ('A_SHARE', 'HK', 'US', 'ETF', 'FUND')),
  name VARCHAR(128) NOT NULL DEFAULT '',
  sort_order SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_watchlist_user_symbol UNIQUE (user_id, symbol)
);

CREATE INDEX idx_watchlist_user_id ON t_watchlist (user_id);
CREATE INDEX idx_watchlist_group_id ON t_watchlist (group_id);

COMMENT ON TABLE t_watchlist IS '自选股';
COMMENT ON COLUMN t_watchlist.id IS '记录主键';
COMMENT ON COLUMN t_watchlist.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_watchlist.group_id IS '关联 t_watchlist_group.id';
COMMENT ON COLUMN t_watchlist.symbol IS '股票代码';
COMMENT ON COLUMN t_watchlist.market IS '市场类型：A_SHARE=A股 HK=港股 US=美股 ETF=ETF FUND=基金';
COMMENT ON COLUMN t_watchlist.name IS '股票名称';
COMMENT ON COLUMN t_watchlist.sort_order IS '排序序号';
COMMENT ON COLUMN t_watchlist.created_at IS '创建时间';
COMMENT ON COLUMN t_watchlist.updated_at IS '更新时间';

CREATE TRIGGER update_t_watchlist_updated_at
BEFORE UPDATE ON t_watchlist
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
