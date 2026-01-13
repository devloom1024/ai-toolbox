# Python Services Gateway

统一接入 akshare, yfinance, tushare 等 Python 库的网关服务，为 Java 后端提供数据服务。

## 特性

- 🚀 **FastAPI 驱动** - 高性能异步 Web 框架
- 📦 **UV 包管理** - 快速、现代化的 Python 包管理器
- 🔌 **模块化架构** - 可插拔的数据源模块
- 📊 **多数据源支持** - Akshare、yfinance、Tushare
- 🔄 **缓存与限流** - Redis 集成，支持缓存和请求限流
- 📝 **结构化日志** - Structlog + JSON Logger
- 🐳 **Docker 支持** - 开箱即用的容器化部署
- 📖 **自动文档** - Swagger UI 和 ReDoc

## 架构

```
python-services/
├── gateway/                 # 主应用
│   ├── app.py              # FastAPI 应用工厂
│   ├── config.py           # 配置管理
│   ├── modules/            # 数据源模块
│   │   ├── base.py         # 模块基类
│   │   ├── __init__.py     # 模块注册表
│   │   ├── akshare/        # Akshare 模块
│   │   ├── yfinance/       # yfinance 模块 (待实现)
│   │   └── tushare/        # Tushare 模块 (待实现)
│   ├── routers/            # API 路由
│   │   ├── __init__.py     # 路由设置
│   │   ├── health.py       # 健康检查
│   │   └── akshare.py      # Akshare API
│   ├── middleware/         # 中间件
│   └── utils/              # 工具函数
├── pyproject.toml          # UV 项目配置
├── uv.toml                 # UV 配置
├── Dockerfile              # Docker 镜像构建
├── docker-compose.yml      # Docker Compose 编排
└── README.md               # 本文档

```

## 快速开始

### 本地开发

#### 1. 安装 UV

```bash
# macOS/Linux
curl -LsSf https://astral.sh/uv/install.sh | sh

# Windows
powershell -c "irm https://astral.sh/uv/install.ps1 | iex"
```

#### 2. 安装依赖

```bash
cd python-services

# 创建虚拟环境并安装所有依赖
uv venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate

# 安装项目依赖 (包括 akshare)
uv pip install -e ".[akshare,all]"
```

#### 3. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 文件修改配置
```

#### 4. 启动服务

```bash
# 开发模式 (支持热重载)
uvicorn gateway.app:app --reload --host 0.0.0.0 --port 8081

# 生产模式
uvicorn gateway.app:app --host 0.0.0.0 --port 8081 --workers 4
```

#### 5. 访问文档

- **Swagger UI**: http://localhost:8081/docs
- **ReDoc**: http://localhost:8081/redoc
- **健康检查**: http://localhost:8081/api/v1/health

### Docker 部署

#### 1. 使用 Docker Compose (推荐)

```bash
cd python-services

# 启动所有服务 (Python Gateway + Redis)
docker-compose up -d

# 查看日志
docker-compose logs -f python-gateway

# 停止服务
docker-compose down
```

#### 2. 手动构建和运行

```bash
# 构建镜像
docker build -t python-gateway:latest .

# 运行容器
docker run -d \
  --name python-gateway \
  -p 8081:8081 \
  -e REDIS_URL=redis://host.docker.internal:6379 \
  python-gateway:latest
```

## API 接口

### 健康检查

```bash
# 检查所有模块健康状态
GET /api/v1/health

# 列出所有已注册的模块
GET /api/v1/health/modules
```

### Akshare 数据接口

```bash
# 获取实时行情
GET /api/v1/akshare/quote/realtime?symbol=600000

# 获取 K 线数据
GET /api/v1/akshare/quote/kline?symbol=600000&period=daily&start_date=20240101&end_date=20241231

# 搜索股票
GET /api/v1/akshare/search?keyword=茅台

# 获取公司信息
GET /api/v1/akshare/fundamental/company?symbol=600000

# 获取财务指标
GET /api/v1/akshare/fundamental/financial?symbol=600000

# 获取资金流向
GET /api/v1/akshare/capital/flow?symbol=600000
```

## 配置说明

### 环境变量

| 变量名                  | 默认值               | 说明                     |
| ----------------------- | -------------------- | ------------------------ |
| `HOST`                  | `0.0.0.0`            | 服务监听地址             |
| `PORT`                  | `8081`               | 服务端口                 |
| `DEBUG`                 | `false`              | 调试模式                 |
| `LOG_LEVEL`             | `INFO`               | 日志级别                 |
| `REDIS_URL`             | `redis://localhost:6379` | Redis 连接 URL       |
| `TIMEOUT_AKSHARE`       | `5`                  | Akshare 超时时间 (秒)    |
| `TIMEOUT_YFINANCE`      | `10`                 | yfinance 超时时间 (秒)   |
| `RATE_LIMIT_REQUESTS`   | `60`                 | 每分钟请求数             |
| `RATE_LIMIT_WINDOW`     | `60`                 | 限流窗口时间 (秒)        |

### 可选依赖

```bash
# 只安装 Akshare
uv pip install -e ".[akshare]"

# 只安装 yfinance
uv pip install -e ".[yfinance]"

# 安装所有数据源
uv pip install -e ".[all]"

# 开发依赖
uv pip install -e ".[dev]"
```

## 开发指南

### 添加新的数据源模块

1. **创建模块目录**

```bash
mkdir -p gateway/modules/newmodule
touch gateway/modules/newmodule/{__init__.py,client.py,module.py}
```

2. **实现模块类** (继承 `BaseModule`)

```python
# gateway/modules/newmodule/module.py
from ..base import BaseModule

class NewModule(BaseModule):
    def __init__(self):
        super().__init__("newmodule")

    async def initialize(self) -> None:
        # 初始化逻辑
        self._initialized = True

    async def health_check(self) -> dict:
        return {"status": "healthy", "module": self.name}

    async def get_version(self) -> str:
        return "1.0.0"
```

3. **添加 API 路由**

```python
# gateway/routers/newmodule.py
from fastapi import APIRouter

router = APIRouter()

@router.get("/data")
async def get_data():
    return {"message": "Hello from new module"}
```

4. **注册模块**

```python
# gateway/app.py
from .modules.newmodule import NewModule

# 在 lifespan 函数中注册
new_module = NewModule()
registry.register(new_module)
```

5. **注册路由**

```python
# gateway/routers/__init__.py
from . import newmodule

api_router.include_router(newmodule.router, prefix="/newmodule", tags=["newmodule"])
```

### 代码风格

使用 Ruff 进行代码检查和格式化:

```bash
# 检查代码
uv run ruff check gateway/

# 自动修复
uv run ruff check --fix gateway/

# 格式化代码
uv run ruff format gateway/
```

### 类型检查

使用 Mypy 进行类型检查:

```bash
uv run mypy gateway/
```

### 运行测试

```bash
# 运行所有测试
uv run pytest

# 运行特定测试
uv run pytest gateway/tests/test_akshare.py

# 生成覆盖率报告
uv run pytest --cov=gateway --cov-report=html
```

## 与 Java 后端集成

Java 后端可以通过 HTTP 客户端调用 Python 服务:

```java
// AkshareClient.java
@Component
public class AkshareClient {
    private final RestTemplate restTemplate;

    @Value("${python.gateway.url}")
    private String gatewayUrl;

    public QuoteData getRealtimeQuote(String symbol) {
        String url = gatewayUrl + "/api/v1/akshare/quote/realtime?symbol=" + symbol;
        return restTemplate.getForObject(url, QuoteData.class);
    }
}
```

配置示例:

```yaml
# application.yml
python:
  gateway:
    url: http://localhost:8081
    timeout: 10s
```

## 性能优化

- **连接池**: 使用 `httpx` 的 `AsyncClient` 复用连接
- **缓存策略**: Redis 缓存热点数据，减少数据源请求
- **并发控制**: FastAPI 异步处理，支持高并发
- **限流保护**: 防止过载，保护下游数据源

## 监控与日志

### 结构化日志

所有日志以 JSON 格式输出，便于采集和分析:

```json
{
  "timestamp": "2024-01-13T10:30:00.123Z",
  "level": "info",
  "module": "akshare",
  "method": "get_realtime_quote",
  "params": {"symbol": "600000"},
  "duration_ms": 150
}
```

### 健康检查

```bash
curl http://localhost:8081/api/v1/health
```

响应示例:

```json
{
  "status": "healthy",
  "modules": {
    "akshare": {
      "status": "healthy",
      "test_result": "ok"
    }
  }
}
```

## 故障排查

### 常见问题

**1. 端口冲突**

```bash
# 修改 .env 或 docker-compose.yml 中的端口配置
PORT=8081
```

**2. Redis 连接失败**

```bash
# 检查 Redis 是否运行
docker-compose ps redis

# 查看 Redis 日志
docker-compose logs redis
```

**3. 模块初始化失败**

```bash
# 查看应用日志
docker-compose logs python-gateway

# 检查依赖是否安装
uv pip list | grep akshare
```

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request!
