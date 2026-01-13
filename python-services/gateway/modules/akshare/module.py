"""
Akshare 模块实现
"""
from typing import Any

from ..base import BaseModule
from .client import AkshareClient


class AkshareModule(BaseModule):
    """Akshare 数据源模块"""

    def __init__(self):
        super().__init__("akshare")
        self.client = AkshareClient()

    async def initialize(self) -> None:
        """初始化模块"""
        if self._initialized:
            return

        await self.client.initialize()
        self._initialized = True

    async def health_check(self) -> dict[str, Any]:
        """健康检查"""
        try:
            # 尝试获取实时行情测试连接
            result = self.client.get_realtime_quote("sh600000")
            return {
                "status": "healthy",
                "module": self.name,
                "test_result": "ok" if result is not None else "empty",
            }
        except Exception as e:
            return {"status": "unhealthy", "module": self.name, "error": str(e)}

    async def get_version(self) -> str:
        """获取 akshare 版本"""
        try:
            import akshare

            return akshare.__version__
        except Exception:
            return "unknown"

    async def cleanup(self) -> None:
        """清理资源"""
        self._initialized = False
