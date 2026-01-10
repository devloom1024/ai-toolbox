# 投资模块数据库设计

> 本设计遵循 [PostgreSQL 规范](../../specs/postgresql.md)

## 一、ER 关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                      数据库 ER 关系图                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────────┐                                               │
│   │  t_user     │                                               │
│   └──────┬──────┘                                               │
│          │ 1:N                                                  │
│          ▼                                                      │
│   ┌─────────────┐         ┌─────────────┐                      │
│   │  t_account  │────────▶│ t_watchlist │                      │
│   │  投资账号   │  1:N    │  自选股     │                      │
│   └─────────────┘         └─────────────┘                      │
│          │                        │                             │
│          │ 1:N                    │ 1:N                         │
│          ▼                        ▼                             │
│   ┌─────────────┐         ┌─────────────────┐                  │
│   │ t_position  │         │ t_watchlist_group│                 │
│   │  持仓       │         │  自选分组        │                 │
│   └──────┬──────┘         └─────────────────┘                  │
│          │                                                    │
│          │ 1:N                                                │
│          ▼                                                    │
│   ┌─────────────────────┐                                     │
│   │ t_position_trade    │                                     │
│   │  交易记录(加仓/减仓) │                                     │
│   └─────────────────────┘                                     │
│                                                                 │
│   ┌─────────────┐         ┌─────────────────┐                  │
│   │  t_user     │────────▶│ t_strategy      │                  │
│   │  用户       │  1:N    │  策略           │                  │
│   └─────────────┘         └─────────────────┘                  │
│                                  │                              │
│                                  │ 1:N                          │
│                                  ▼                              │
│                        ┌─────────────────────┐                 │
│                        │ t_strategy_condition│                 │
│                        │  策略条件           │                 │
│                        └─────────────────────┘                 │
│                                                                 │
│   ┌─────────────┐         ┌─────────────────┐                  │
│   │  t_user     │────────▶│ t_analysis_record│                 │
│   │  用户       │  1:N    │  AI 分析记录     │                  │
│   └─────────────┘         └─────────────────┘                  │
│                                                                 │
│   ┌─────────────┐         ┌─────────────────┐                  │
│   │  t_strategy │         │ t_backtest_result│                 │
│   │  策略       │  1:N    │  回测结果        │                  │
│   └─────────────┘         └─────────────────┘                  │
│                                                                 │
│   ┌─────────────┐         ┌─────────────┐                      │
│   │  t_user     │────────▶│ t_push_history│                    │
│   │  用户       │  1:N    │  推送历史    │                      │
│   └─────────────┘         └─────────────┘                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 二、通用触发器函数

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

## 三、表结构设计

### 3.0 t_account - 投资账号表（新增）

```sql
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
```

### 3.1 t_investment_user_setting - 用户投资设置

```sql
CREATE TABLE t_investment_user_setting (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  risk_tolerance VARCHAR(16) NOT NULL DEFAULT 'MODERATE' 
    CHECK (risk_tolerance IN ('CONSERVATIVE', 'MODERATE', 'AGGRESSIVE')),
  investment_goal VARCHAR(32) NOT NULL DEFAULT 'PRESERVATION' 
    CHECK (investment_goal IN ('PRESERVATION', 'GROWTH')),
  experience_level VARCHAR(16) NOT NULL DEFAULT 'BEGINNER' 
    CHECK (experience_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
  push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
  push_config JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_investment_setting_user UNIQUE (user_id)
);

CREATE INDEX idx_investment_setting_user_id ON t_investment_user_setting (user_id);

-- 添加注释
COMMENT ON TABLE t_investment_user_setting IS '用户投资设置';
COMMENT ON COLUMN t_investment_user_setting.id IS '记录主键';
COMMENT ON COLUMN t_investment_user_setting.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_investment_user_setting.risk_tolerance IS '风险承受：CONSERVATIVE=保守 MODERATE=稳健 AGGRESSIVE=激进';
COMMENT ON COLUMN t_investment_user_setting.investment_goal IS '投资目标：PRESERVATION=保值增值 GROWTH=追求高收益';
COMMENT ON COLUMN t_investment_user_setting.experience_level IS '经验水平：BEGINNER=入门 INTERMEDIATE=中级 ADVANCED=高级';
COMMENT ON COLUMN t_investment_user_setting.push_enabled IS '是否开启消息推送';
COMMENT ON COLUMN t_investment_user_setting.push_config IS '推送配置 JSON';
COMMENT ON COLUMN t_investment_user_setting.created_at IS '创建时间';
COMMENT ON COLUMN t_investment_user_setting.updated_at IS '更新时间';

-- 创建触发器
CREATE TRIGGER update_t_investment_user_setting_updated_at
BEFORE UPDATE ON t_investment_user_setting
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### 3.2 t_watchlist_group - 自选股分组

```sql
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
```

### 3.3 t_watchlist - 自选股

```sql
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
```

### 3.4 t_position - 持仓（关联账号版）

```sql
CREATE TABLE t_position (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  symbol VARCHAR(32) NOT NULL,
  market VARCHAR(16) NOT NULL,
  name VARCHAR(128) NOT NULL DEFAULT '',
  position_type VARCHAR(16) NOT NULL 
    CHECK (position_type IN ('STOCK', 'ETF', 'FUND')),
  
  -- 股票/ETF 字段（STOCK/ETF 使用）
  shares DECIMAL(18, 4),
  avg_cost DECIMAL(18, 4),
  
  -- 基金字段（FUND 使用）
  amount DECIMAL(18, 2),
  profit DECIMAL(18, 2),
  
  current_price DECIMAL(18, 4),
  current_value DECIMAL(18, 2),
  profit_rate DECIMAL(10, 4),
  
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_position_user_id ON t_position (user_id);
CREATE INDEX idx_position_account_id ON t_position (account_id);

COMMENT ON TABLE t_position IS '持仓';
COMMENT ON COLUMN t_position.id IS '持仓主键';
COMMENT ON COLUMN t_position.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_position.account_id IS '关联 t_account.id';
COMMENT ON COLUMN t_position.symbol IS '股票/基金代码';
COMMENT ON COLUMN t_position.market IS '市场类型：A_SHARE=A股 HK=港股 US=美股 ETF=ETF基金 FUND=场外基金';
COMMENT ON COLUMN t_position.name IS '股票/基金名称';
COMMENT ON COLUMN t_position.position_type IS '持仓类型：STOCK=股票 ETF=ETF基金 FUND=场外基金';
COMMENT ON COLUMN t_position.shares IS '持仓股数（position_type=STOCK 或 ETF 时使用，录入份额）';
COMMENT ON COLUMN t_position.avg_cost IS '平均成本价（position_type=STOCK 或 ETF 时使用，单位：元/股）';
COMMENT ON COLUMN t_position.amount IS '持仓金额（position_type=FUND 时使用，单位：元）';
COMMENT ON COLUMN t_position.profit IS '持仓收益（position_type=FUND 时使用，单位：元）';
COMMENT ON COLUMN t_position.current_price IS '当前净值/价格（ETF 和 FUND 使用净值，STOCK 使用价格）';
COMMENT ON COLUMN t_position.current_value IS '当前市值 = 份额×现价 或 金额+收益';
COMMENT ON COLUMN t_position.profit_rate IS '收益率 = (当前市值-成本)/成本';
COMMENT ON COLUMN t_position.created_at IS '创建时间';
COMMENT ON COLUMN t_position.updated_at IS '更新时间';

CREATE TRIGGER update_t_position_updated_at
BEFORE UPDATE ON t_position
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### 3.5 t_position_trade - 持仓交易记录（新增）

```sql
CREATE TABLE t_position_trade (
  id BIGSERIAL PRIMARY KEY,
  position_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  symbol VARCHAR(32) NOT NULL,
  trade_type VARCHAR(16) NOT NULL 
    CHECK (trade_type IN ('BUY', 'SELL')),
  trade_price DECIMAL(18, 4) NOT NULL,
  trade_shares DECIMAL(18, 4),
  trade_amount DECIMAL(18, 2),
  trade_fee DECIMAL(18, 2) NOT NULL DEFAULT 0,
  notes VARCHAR(255) NOT NULL DEFAULT '',
  trade_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_position_trade_position_id ON t_position_trade (position_id);
CREATE INDEX idx_position_trade_account_id ON t_position_trade (account_id);
CREATE INDEX idx_position_trade_user_id ON t_position_trade (user_id);
CREATE INDEX idx_position_trade_trade_at ON t_position_trade (trade_at DESC);

COMMENT ON TABLE t_position_trade IS '持仓交易记录（加仓/减仓记录）';
COMMENT ON COLUMN t_position_trade.id IS '交易记录主键';
COMMENT ON COLUMN t_position_trade.position_id IS '关联 t_position.id';
COMMENT ON COLUMN t_position_trade.account_id IS '关联 t_account.id';
COMMENT ON COLUMN t_position_trade.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_position_trade.symbol IS '股票/基金代码';
COMMENT ON COLUMN t_position_trade.trade_type IS '交易类型：BUY=买入/加仓 SELL=卖出/减仓';
COMMENT ON COLUMN t_position_trade.trade_price IS '成交价格/净值';
COMMENT ON COLUMN t_position_trade.trade_shares IS '成交份额（股票/ETF 使用，单位：股）';
COMMENT ON COLUMN t_position_trade.trade_amount IS '成交金额（基金使用，单位：元）';
COMMENT ON COLUMN t_position_trade.trade_fee IS '交易费用（手续费等，单位：元）';
COMMENT ON COLUMN t_position_trade.notes IS '备注（可选，记录加仓/减仓原因等）';
COMMENT ON COLUMN t_position_trade.trade_at IS '交易时间';
COMMENT ON COLUMN t_position_trade.created_at IS '创建时间';
```

### 3.6 t_strategy - 策略

```sql
CREATE TABLE t_strategy (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  description TEXT NOT NULL DEFAULT '',
  type VARCHAR(32) NOT NULL 
    CHECK (type IN ('TECHNICAL', 'FUNDAMENTAL', 'MIXED', 'CUSTOM')),
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' 
    CHECK (status IN ('DRAFT', 'BACKTESTING', 'ENABLED', 'DISABLED')),
  script_content TEXT,
  backtest_config JSONB NOT NULL DEFAULT '{}',
  last_backtest_at TIMESTAMPTZ,
  last_backtest_result JSONB,
  enabled_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_strategy_user_id ON t_strategy (user_id);
CREATE INDEX idx_strategy_status ON t_strategy (status);

COMMENT ON TABLE t_strategy IS '交易策略';
COMMENT ON COLUMN t_strategy.id IS '策略主键';
COMMENT ON COLUMN t_strategy.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_strategy.name IS '策略名称';
COMMENT ON COLUMN t_strategy.description IS '策略描述';
COMMENT ON COLUMN t_strategy.type IS '策略类型：TECHNICAL=技术指标策略 FUNDAMENTAL=基本面策略 MIXED=混合策略 CUSTOM=自定义脚本策略';
COMMENT ON COLUMN t_strategy.status IS '状态：DRAFT=草稿 BACKTESTING=回测中 ENABLED=已启用 DISABLED=已禁用';
COMMENT ON COLUMN t_strategy.script_content IS '自定义脚本内容（type=CUSTOM 时使用）';
COMMENT ON COLUMN t_strategy.backtest_config IS '回测配置 JSON';
COMMENT ON COLUMN t_strategy.last_backtest_at IS '最后回测时间';
COMMENT ON COLUMN t_strategy.last_backtest_result IS '最后回测结果 JSON';
COMMENT ON COLUMN t_strategy.enabled_at IS '启用时间';
COMMENT ON COLUMN t_strategy.created_at IS '创建时间';
COMMENT ON COLUMN t_strategy.updated_at IS '更新时间';

CREATE TRIGGER update_t_strategy_updated_at
BEFORE UPDATE ON t_strategy
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### 3.6 t_strategy_condition - 策略条件

```sql
CREATE TABLE t_strategy_condition (
  id BIGSERIAL PRIMARY KEY,
  strategy_id BIGINT NOT NULL,
  condition_type VARCHAR(32) NOT NULL 
    CHECK (condition_type IN ('INDICATOR', 'PRICE', 'VOLUME', 'FUNDAMENTAL', 'CUSTOM')),
  indicator_name VARCHAR(64),
  operator VARCHAR(16) NOT NULL 
    CHECK (operator IN ('GT', 'LT', 'GTE', 'LTE', 'EQ', 'CROSS_UP', 'CROSS_DOWN')),
  threshold_value DECIMAL(18, 4),
  threshold_indicator VARCHAR(64),
  threshold_multiplier DECIMAL(8, 4),
  logic_operator VARCHAR(8) NOT NULL DEFAULT 'AND' 
    CHECK (logic_operator IN ('AND', 'OR')),
  sort_order SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_strategy_condition_strategy_id ON t_strategy_condition (strategy_id);

COMMENT ON TABLE t_strategy_condition IS '策略条件';
COMMENT ON COLUMN t_strategy_condition.id IS '条件主键';
COMMENT ON COLUMN t_strategy_condition.strategy_id IS '关联 t_strategy.id';
COMMENT ON COLUMN t_strategy_condition.condition_type IS '条件类型：INDICATOR=技术指标 PRICE=价格条件 VOLUME=成交量条件 FUNDAMENTAL=基本面条件 CUSTOM=自定义脚本';
COMMENT ON COLUMN t_strategy_condition.indicator_name IS '指标名称（如 MA20、RSI14、MACD 等）';
COMMENT ON COLUMN t_strategy_condition.operator IS '运算符：GT=大于 LT=小于 GTE=大于等于 LTE=小于等于 EQ=等于 CROSS_UP=上穿 CROSS_DOWN=下穿';
COMMENT ON COLUMN t_strategy_condition.threshold_value IS '阈值（固定数值）';
COMMENT ON COLUMN t_strategy_condition.threshold_indicator IS '阈值指标（与另一指标比较时使用）';
COMMENT ON COLUMN t_strategy_condition.threshold_multiplier IS '阈值乘数（如 MA5×1.5）';
COMMENT ON COLUMN t_strategy_condition.logic_operator IS '逻辑运算符：AND=并且 OR=或者（与前一个条件的逻辑关系）';
COMMENT ON COLUMN t_strategy_condition.sort_order IS '排序序号';
COMMENT ON COLUMN t_strategy_condition.created_at IS '创建时间';
```

### 3.7 t_analysis_record - AI 分析记录

```sql
CREATE TABLE t_analysis_record (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  symbol VARCHAR(32) NOT NULL,
  market VARCHAR(16) NOT NULL,
  analysis_type VARCHAR(32) NOT NULL 
    CHECK (analysis_type IN ('BUILD_POSITION', 'HOLD', 'SECTOR', 'PORTFOLIO')),
  recommendation VARCHAR(16) NOT NULL,
  confidence DECIMAL(5, 2) NOT NULL,
  analysis_result JSONB NOT NULL,
  analysis_content TEXT,
  data_sources JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_analysis_record_user_id ON t_analysis_record (user_id);
CREATE INDEX idx_analysis_record_symbol ON t_analysis_record (symbol);
CREATE INDEX idx_analysis_record_created_at ON t_analysis_record (created_at DESC);

COMMENT ON TABLE t_analysis_record IS 'AI 分析记录';
COMMENT ON COLUMN t_analysis_record.id IS '记录主键';
COMMENT ON COLUMN t_analysis_record.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_analysis_record.symbol IS '股票/基金代码';
COMMENT ON COLUMN t_analysis_record.market IS '市场类型：A_SHARE=A股 HK=港股 US=美股 ETF=ETF基金 FUND=场外基金';
COMMENT ON COLUMN t_analysis_record.analysis_type IS '分析类型：BUILD_POSITION=建仓分析 HOLD=持仓分析 SECTOR=板块分析 PORTFOLIO=组合分析';
COMMENT ON COLUMN t_analysis_record.recommendation IS '建议：BUILD_POSITION=建仓 HOLD=持有 WAIT=观望 AVOID=回避 SELL=卖出';
COMMENT ON COLUMN t_analysis_record.confidence IS '置信度 0-100';
COMMENT ON COLUMN t_analysis_record.analysis_result IS '分析结果 JSON（多维度评分、关键数据等）';
COMMENT ON COLUMN t_analysis_record.analysis_content IS 'AI 分析内容文本（推理过程）';
COMMENT ON COLUMN t_analysis_record.data_sources IS '数据来源 JSON（akshare、yfinance 等）';
COMMENT ON COLUMN t_analysis_record.created_at IS '分析时间';
```

### 3.8 t_backtest_result - 回测结果

```sql
CREATE TABLE t_backtest_result (
  id BIGSERIAL PRIMARY KEY,
  strategy_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  initial_capital DECIMAL(18, 2) NOT NULL,
  final_capital DECIMAL(18, 2) NOT NULL,
  total_return DECIMAL(10, 4) NOT NULL,
  max_drawdown DECIMAL(10, 4) NOT NULL,
  sharpe_ratio DECIMAL(10, 4),
  win_rate DECIMAL(10, 4),
  trade_count INTEGER NOT NULL,
  metrics_detail JSONB NOT NULL DEFAULT '{}',
  trade_records JSONB NOT NULL DEFAULT '[]',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_backtest_result_strategy_id ON t_backtest_result (strategy_id);
CREATE INDEX idx_backtest_result_user_id ON t_backtest_result (user_id);

COMMENT ON TABLE t_backtest_result IS '回测结果';
COMMENT ON COLUMN t_backtest_result.id IS '结果主键';
COMMENT ON COLUMN t_backtest_result.strategy_id IS '关联 t_strategy.id';
COMMENT ON COLUMN t_backtest_result.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_backtest_result.start_date IS '回测开始日期';
COMMENT ON COLUMN t_backtest_result.end_date IS '回测结束日期';
COMMENT ON COLUMN t_backtest_result.initial_capital IS '初始资金';
COMMENT ON COLUMN t_backtest_result.final_capital IS '最终资金';
COMMENT ON COLUMN t_backtest_result.total_return IS '总收益率';
COMMENT ON COLUMN t_backtest_result.max_drawdown IS '最大回撤';
COMMENT ON COLUMN t_backtest_result.sharpe_ratio IS '夏普比率';
COMMENT ON COLUMN t_backtest_result.win_rate IS '胜率';
COMMENT ON COLUMN t_backtest_result.trade_count IS '交易次数';
COMMENT ON COLUMN t_backtest_result.metrics_detail IS '详细指标 JSON';
COMMENT ON COLUMN t_backtest_result.trade_records IS '交易记录 JSON';
COMMENT ON COLUMN t_backtest_result.created_at IS '回测时间';
```

### 3.9 t_push_history - 推送历史

```sql
CREATE TABLE t_push_history (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  push_type VARCHAR(32) NOT NULL 
    CHECK (push_type IN ('DAILY_DIAGNOSIS', 'BUILD_RECOMMEND', 'ALERT', 'STRATEGY_SIGNAL', 'SECTOR_REPORT', 'MONTHLY_REPORT')),
  title VARCHAR(256) NOT NULL,
  content TEXT NOT NULL,
  symbols JSONB,
  sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_push_history_user_id ON t_push_history (user_id);
CREATE INDEX idx_push_history_sent_at ON t_push_history (sent_at DESC);

COMMENT ON TABLE t_push_history IS '推送历史';
COMMENT ON COLUMN t_push_history.id IS '记录主键';
COMMENT ON COLUMN t_push_history.user_id IS '关联 t_user.id';
COMMENT ON COLUMN t_push_history.push_type IS '推送类型：DAILY_DIAGNOSIS=每日持仓诊断 BUILD_RECOMMEND=建仓推荐 ALERT=异动提醒 STRATEGY_SIGNAL=策略信号 SECTOR_REPORT=热门板块周报 MONTHLY_REPORT=AI投资月报';
COMMENT ON COLUMN t_push_history.title IS '推送标题';
COMMENT ON COLUMN t_push_history.content IS '推送内容（HTML 或 Markdown 格式）';
COMMENT ON COLUMN t_push_history.symbols IS '相关股票代码 JSON 数组';
COMMENT ON COLUMN t_push_history.sent_at IS '发送时间';
COMMENT ON COLUMN t_push_history.read IS '是否已读';
```

## 四、Flyway 迁移文件命名

```
db/migration/
├── V1__init_investment_schema.sql          -- 初始化投资模块表结构
├── V2__add_investment_indexes.sql          -- 添加索引优化
└── V3__add_investment_data.sql             -- 初始化基础数据
```

## 五、设计说明

### 5.1 遵循的规范

- **命名规范**：表名使用小写 + 下划线，字段名使用小写 + 下划线
- **主键设计**：使用 BIGSERIAL 自增主键
- **时间字段**：统一使用 TIMESTAMPTZ（带时区）
- **枚举存储**：使用 VARCHAR + CHECK 约束
- **JSONB 使用**：用于存储灵活的配置和结果数据
- **索引设计**：根据查询模式创建必要的索引
- **触发器**：使用触发器自动更新 updated_at 字段

### 5.2 数据安全

- **不适用外键**：通过应用层控制关联关系
- **软删除**：通过状态字段标识（status）
- **数据加密**：敏感信息可在应用层加密后存储

### 5.3 性能优化

- **部分索引**：考虑对大表创建部分索引
- **JSONB 索引**：对频繁查询的 JSONB 字段创建 GIN 索引
- **定期清理**：通过定时任务清理过期数据
