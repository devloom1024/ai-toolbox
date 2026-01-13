"""
FastAPI 应用工厂
"""
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .config import settings
from .modules import registry
from .modules.akshare import AkshareModule
from .routers import setup_routers
from .middleware import setup_middleware
from .utils.logger import logger


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动时
    logger.info("Starting Python Services Gateway...", version="1.0.0")

    # 注册并初始化模块
    try:
        # 注册 Akshare 模块
        akshare_module = AkshareModule()
        registry.register(akshare_module)

        # TODO: 注册其他模块 (yfinance, tushare 等)

        # 初始化所有模块
        await registry.initialize_all()

        logger.info(f"Registered modules: {list(registry._modules.keys())}")
    except Exception as e:
        logger.error("Failed to initialize modules", error=str(e), exc_info=True)
        raise

    yield

    # 关闭时
    logger.info("Shutting down Python Services Gateway...")
    await registry.cleanup_all()


def create_app() -> FastAPI:
    """创建 FastAPI 应用"""
    app = FastAPI(
        title="Python Services Gateway",
        description="统一接入 akshare, yfinance, tushare 等 Python 库，为 Java 后端提供数据服务",
        version="1.0.0",
        lifespan=lifespan,
        docs_url="/docs",
        redoc_url="/redoc",
    )

    # 设置 CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # 设置中间件
    setup_middleware(app)

    # 设置路由
    setup_routers(app)

    return app


# 创建应用实例
app = create_app()
