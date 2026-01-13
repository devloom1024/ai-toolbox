"""
模块注册表
"""
from typing import Any

from .base import BaseModule


class ModuleRegistry:
    """模块注册表 - 管理所有数据源模块"""

    def __init__(self):
        self._modules: dict[str, BaseModule] = {}

    def register(self, module: BaseModule) -> None:
        """
        注册模块

        Args:
            module: 模块实例
        """
        if module.name in self._modules:
            raise ValueError(f"Module '{module.name}' already registered")
        self._modules[module.name] = module

    def get(self, name: str) -> BaseModule:
        """
        获取模块

        Args:
            name: 模块名称

        Returns:
            模块实例

        Raises:
            KeyError: 模块不存在
        """
        if name not in self._modules:
            raise KeyError(f"Module '{name}' not found")
        return self._modules[name]

    def get_all(self) -> dict[str, BaseModule]:
        """获取所有注册的模块"""
        return self._modules.copy()

    async def initialize_all(self) -> None:
        """初始化所有模块"""
        for module in self._modules.values():
            if not module.is_initialized():
                await module.initialize()

    async def cleanup_all(self) -> None:
        """清理所有模块"""
        for module in self._modules.values():
            await module.cleanup()

    async def health_check_all(self) -> dict[str, Any]:
        """
        检查所有模块的健康状态

        Returns:
            所有模块的健康状态
        """
        results = {}
        for name, module in self._modules.items():
            try:
                results[name] = await module.health_check()
            except Exception as e:
                results[name] = {"status": "unhealthy", "error": str(e)}
        return results


# 全局注册表实例
registry = ModuleRegistry()
