"""
配置管理
"""
from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    """应用配置"""

    # 服务配置
    host: str = "0.0.0.0"
    port: int = 8080
    debug: bool = False

    # 日志配置
    log_level: str = "INFO"
    log_format: str = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"

    # Redis 配置 (用于缓存/限流)
    redis_url: str = "redis://localhost:6379"

    # 超时配置 (秒)
    timeout_default: int = 10
    timeout_akshare: int = 5
    timeout_yfinance: int = 10

    # 限流配置
    rate_limit_requests: int = 60  # 每分钟请求数
    rate_limit_window: int = 60    # 窗口时间(秒)

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


@lru_cache()
def get_settings() -> Settings:
    """获取配置单例"""
    return Settings()


settings = get_settings()
