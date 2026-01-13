"""
健康检查路由
"""
from fastapi import APIRouter
from pydantic import BaseModel

from ..modules import registry

router = APIRouter()


class HealthResponse(BaseModel):
    """健康检查响应"""

    status: str
    modules: dict[str, dict]


@router.get("", response_model=HealthResponse)
async def health_check():
    """
    健康检查接口

    检查所有已注册模块的健康状态
    """
    modules = await registry.health_check_all()

    # 判断整体状态
    all_healthy = all(
        m.get("status") == "healthy" for m in modules.values() if isinstance(m, dict)
    )
    overall_status = "healthy" if all_healthy else "degraded"

    return HealthResponse(status=overall_status, modules=modules)


@router.get("/modules")
async def list_modules():
    """
    列出所有已注册的模块

    Returns:
        所有模块的名称和版本信息
    """
    modules = registry.get_all()
    return {
        "modules": [
            {"name": name, "version": await module.get_version(), "initialized": module.is_initialized()}
            for name, module in modules.items()
        ]
    }
