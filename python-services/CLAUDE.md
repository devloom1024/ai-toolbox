# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with this repository.

## 项目概述

Python Services Gateway - 基于 FastAPI 的金融数据网关服务,统一接入 akshare、yfinance、tushare 等 Python 库,为 Java 后端提供数据服务。

## 常用命令

```bash
# 安装依赖
cd python-services
uv venv
source .venv/bin/activate
uv pip install -e ".[akshare,all]"    # 安装所有数据源
uv pip install -e ".[dev]"            # 安装开发依赖

# 启动服务
python run.py                         # 开发模式 (热重载)
uvicorn gateway.app:app --reload --host 0.0.0.0 --port 8081

# 代码检查与格式化
uv run ruff check gateway/            # 检查
uv run ruff check --fix gateway/      # 自动修复
uv run ruff format gateway/           # 格式化

# 类型检查
uv run mypy gateway/

# 运行测试
uv run pytest
uv run pytest gateway/tests/test_akshare.py

# Docker 部署
docker-compose up -d
```

## 架构概览

```
gateway/
├── app.py              # FastAPI 应用工厂,生命周期管理
├── config.py           # pydantic-settings 配置管理
├── modules/            # 数据源模块 (插件化架构)
│   ├── base.py         # BaseModule 抽象基类
│   ├── __init__.py     # ModuleRegistry 模块注册表
│   └── akshare/        # Akshare 模块示例
│       ├── module.py   # 模块类 (继承 BaseModule)
│       ├── client.py   # 客户端封装 (调用 akshare 库)
│       └── mapper.py   # 数据映射 (DataFrame -> JSON)
├── routers/            # API 路由
│   ├── __init__.py     # 路由注册
│   ├── akshare.py      # Akshare API 端点
│   └── health.py       # 健康检查
├── middleware/         # 中间件
│   └── __init__.py     # 请求日志等
└── utils/              # 工具函数
    └── logger.py       # structlog + JSON 日志
```

## 核心模式

### 模块注册机制
- `ModuleRegistry` (单例) 管理所有数据源模块
- 应用启动时在 `lifespan` 函数中注册模块
- 支持动态初始化和清理

### 新增数据源模块流程
1. 创建 `gateway/modules/newmodule/{module.py,client.py,mapper.py,__init__.py}`
2. `module.py`: 继承 `BaseModule`,实现 `initialize/health_check/get_version` 方法
3. `client.py`: 封装第三方库的异步/同步调用
4. `mapper.py`: 将 pandas DataFrame 转换为 API 响应格式
5. 在 `app.py` 的 `lifespan` 中注册模块
6. 在 `routers/__init__.py` 中注册路由

### API 响应格式
所有接口返回统一的 `QuoteResponse` 结构:
```python
class QuoteResponse(BaseModel):
    code: int = 0          # 业务状态码
    message: str = "success"
    data: list[dict] | dict | None
```

## 配置管理

- 使用 `pydantic-settings` + `.env` 文件
- 在 `config.py` 中定义 `Settings` 类
- 通过 `@lru_cache()` 获取配置单例

## 日志规范

- 使用 `structlog` + `pythonjsonlogger` 输出 JSON 格式日志
- `log_api_call()` 记录 API 调用
- `log_error()` 记录错误并包含 `exc_info`

## 依赖管理

- 使用 `uv` 包管理器
- 基础依赖在 `dependencies` 中
- 可选依赖通过 `optional-dependencies` 组管理 (akshare, yfinance, tushare)
