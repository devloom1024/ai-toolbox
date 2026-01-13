"""
路由配置
"""
from fastapi import APIRouter, FastAPI

from . import akshare, health


def setup_routers(app: FastAPI) -> None:
    """配置所有路由"""

    # API 根路由
    api_router = APIRouter(prefix="/api/v1")

    # 注册健康检查路由
    api_router.include_router(health.router, prefix="/health", tags=["health"])

    # 注册模块路由
    api_router.include_router(akshare.router, prefix="/akshare", tags=["akshare"])
    # TODO: 注册其他模块路由 (yfinance, tushare 等)

    # 挂载到应用
    app.include_router(api_router)
