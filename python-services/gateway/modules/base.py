"""
模块基类
"""
from abc import ABC, abstractmethod
from typing import Any


class BaseModule(ABC):
    """所有数据模块的抽象基类"""

    def __init__(self, name: str):
        """
        初始化模块

        Args:
            name: 模块名称
        """
        self.name = name
        self._initialized = False

    @abstractmethod
    async def initialize(self) -> None:
        """
        初始化模块资源（如连接池等）
        子类必须实现此方法
        """
        pass

    @abstractmethod
    async def health_check(self) -> dict[str, Any]:
        """
        健康检查

        Returns:
            健康状态信息
        """
        pass

    @abstractmethod
    async def get_version(self) -> str:
        """
        获取模块版本

        Returns:
            版本号
        """
        pass

    async def cleanup(self) -> None:
        """
        清理模块资源
        子类可选择性重写
        """
        pass

    def is_initialized(self) -> bool:
        """检查模块是否已初始化"""
        return self._initialized

    async def __aenter__(self):
        """异步上下文管理器入口"""
        await self.initialize()
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """异步上下文管理器退出"""
        await self.cleanup()
