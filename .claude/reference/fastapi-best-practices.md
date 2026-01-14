# FastAPI 最佳实践

## 项目配置

- **FastAPI**: 0.109.0+
- **Python**: 3.11
- **包管理器**: uv
- **数据处理**: pandas, numpy
- **金融数据源**: akshare, yfinance, tushare

## 项目结构

```
python-services/
├── gateway/
│   ├── app.py              # FastAPI 应用工厂
│   ├── config.py           # pydantic-settings 配置
│   ├── main.py             # 入口文件
│   ├── modules/            # 数据源模块 (插件化)
│   │   ├── base.py         # BaseModule 抽象基类
│   │   ├── __init__.py     # ModuleRegistry
│   │   ├── akshare/        # Akshare 模块
│   │   │   ├── module.py   # 模块类
│   │   │   ├── client.py   # 客户端封装
│   │   │   └── mapper.py   # 数据映射
│   │   └── yfinance/
│   ├── routers/            # API 路由
│   │   ├── __init__.py     # 路由注册
│   │   ├── health.py       # 健康检查
│   │   └── akshare.py      # 数据 API
│   ├── middleware/         # 中间件
│   │   └── __init__.py
│   └── utils/              # 工具函数
│       └── logger.py       # structlog 日志
├── tests/
│   └── test_*.py
├── pyproject.toml
└── .env
```

## 核心规范

### 统一响应格式
```python
from pydantic import BaseModel
from typing import Optional

class QuoteResponse(BaseModel):
    code: int = 0
    message: str = "success"
    data: Optional[list[dict] | dict] = None

@app.get("/api/v1/quote/stock")
async def get_stock_quote(symbol: str) -> QuoteResponse:
    data = client.get_quote(symbol)
    return QuoteResponse(data=data)
```

### 模块化架构
```python
# gateway/modules/base.py
from abc import ABC, abstractmethod
from typing import Optional

class BaseModule(ABC):
    @abstractmethod
    async def initialize(self) -> None:
        """初始化模块"""

    @abstractmethod
    def health_check(self) -> bool:
        """健康检查"""

    @abstractmethod
    def get_version(self) -> str:
        """获取模块版本"""
```

### 配置管理
```python
# gateway/config.py
from pydantic_settings import BaseSettings
from functools import lru_cache

class Settings(BaseSettings):
    redis_host: str = "localhost"
    redis_port: int = 6379
    akshare_enabled: bool = True

    class Config:
        env_file = ".env"

@lru_cache()
def get_settings() -> Settings:
    return Settings()
```

## 日志规范

```python
import structlog

log = structlog.get_logger()

# API 调用日志
log.info("api_call", endpoint="/quote/stock", symbol="AAPL")

# 错误日志
try:
    result = await fetch_data()
except Exception as e:
    log.error("fetch_failed", error=str(e), exc_info=True)
```

## 开发命令

```bash
# 安装依赖
uv venv
source .venv/bin/activate
uv pip install -e ".[all]"
uv pip install -e ".[dev]"

# 启动服务
python run.py
uvicorn gateway.app:app --reload --host 0.0.0.0 --port 8081

# 代码检查
uv run ruff check gateway/
uv run ruff check --fix gateway/
uv run ruff format gateway/

# 类型检查
uv run mypy gateway/

# 测试
uv run pytest
uv run pytest gateway/tests/
```

## pyproject.toml 配置

```toml
[tool.ruff]
target-version = "py311"
line-length = 100
select = ["E", "F", "I", "UP", "W"]

[tool.mypy]
python_version = "3.11"
warn_return_any = true
disallow_untyped_defs = true

[tool.pytest.ini_options]
asyncio_mode = "auto"
testpaths = ["tests"]
```

## 禁止事项

- ❌ 直接在路由中处理复杂业务逻辑 (使用模块)
- ❌ 不处理异常的异步操作
- ❌ 使用同步阻塞调用外部 API
- ❌ 硬编码配置 (使用 pydantic-settings)
- ❌ 直接返回 pandas DataFrame (转换为 dict)
