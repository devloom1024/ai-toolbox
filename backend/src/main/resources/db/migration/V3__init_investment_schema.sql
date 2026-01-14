-- ============================================
-- Table: t_watchlist_group
-- ============================================
CREATE TABLE t_watchlist_group (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_t_watchlist_group_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_t_watchlist_group_user_id ON t_watchlist_group (user_id);

COMMENT ON TABLE t_watchlist_group IS '自选分组';
COMMENT ON COLUMN t_watchlist_group.id IS '分组主键';
COMMENT ON COLUMN t_watchlist_group.user_id IS '用户 ID';
COMMENT ON COLUMN t_watchlist_group.name IS '分组名称';
COMMENT ON COLUMN t_watchlist_group.is_default IS '是否默认分组';
COMMENT ON COLUMN t_watchlist_group.sort_order IS '排序权重（数值越小越靠前）';
COMMENT ON COLUMN t_watchlist_group.created_at IS '创建时间';
COMMENT ON COLUMN t_watchlist_group.updated_at IS '更新时间';

CREATE TRIGGER update_t_watchlist_group_updated_at
BEFORE UPDATE ON t_watchlist_group
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Table: t_watchlist
-- ============================================
CREATE TABLE t_watchlist (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  symbol VARCHAR(32) NOT NULL,
  name VARCHAR(128) NOT NULL DEFAULT '',
  market VARCHAR(16) NOT NULL CHECK (market IN ('A_SHARE', 'HK', 'US', 'ETF', 'FUND')),
  type VARCHAR(16) NOT NULL CHECK (type IN ('STOCK', 'ETF', 'FUND')),
  note VARCHAR(200) DEFAULT '',
  current_price DECIMAL(20, 4),
  change_percent DECIMAL(10, 4),
  added_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_t_watchlist_user_symbol UNIQUE (user_id, symbol, market)
);

CREATE INDEX idx_t_watchlist_user_id ON t_watchlist (user_id);
CREATE INDEX idx_t_watchlist_group_id ON t_watchlist (group_id);
CREATE INDEX idx_t_watchlist_symbol ON t_watchlist (symbol);

COMMENT ON TABLE t_watchlist IS '自选记录';
COMMENT ON COLUMN t_watchlist.id IS '记录主键';
COMMENT ON COLUMN t_watchlist.user_id IS '用户 ID';
COMMENT ON COLUMN t_watchlist.group_id IS '分组 ID';
COMMENT ON COLUMN t_watchlist.symbol IS '标的代码（纯数字或字母）';
COMMENT ON COLUMN t_watchlist.name IS '标的名称';
COMMENT ON COLUMN t_watchlist.market IS '市场类型：A_SHARE=A股 HK=港股 US=美股 ETF=ETF FUND=基金';
COMMENT ON COLUMN t_watchlist.type IS '标的类型：STOCK=股票 ETF=ETF FUND=基金';
COMMENT ON COLUMN t_watchlist.note IS '备注';
COMMENT ON COLUMN t_watchlist.current_price IS '当前价';
COMMENT ON COLUMN t_watchlist.change_percent IS '涨跌幅（%）';
COMMENT ON COLUMN t_watchlist.added_at IS '添加时间';
