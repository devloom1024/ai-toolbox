"""
结构化日志工具
"""
import logging
import sys
from typing import Any

import structlog
from pythonjsonlogger import jsonlogger

from ..config import settings


def setup_logger() -> structlog.BoundLogger:
    """配置结构化日志"""

    # 配置标准库 logging
    json_handler = logging.StreamHandler(sys.stdout)
    json_handler.setFormatter(
        jsonlogger.JsonFormatter(
            fmt="%(asctime)s %(name)s %(levelname)s %(message)s",
            datefmt="%Y-%m-%d %H:%M:%S",
        )
    )

    root_logger = logging.getLogger()
    root_logger.addHandler(json_handler)
    root_logger.setLevel(settings.log_level)

    # 配置 structlog
    structlog.configure(
        processors=[
            structlog.stdlib.filter_by_level,
            structlog.stdlib.add_logger_name,
            structlog.stdlib.add_log_level,
            structlog.stdlib.PositionalArgumentsFormatter(),
            structlog.processors.TimeStamper(fmt="iso"),
            structlog.processors.StackInfoRenderer(),
            structlog.processors.format_exc_info,
            structlog.processors.UnicodeDecoder(),
            structlog.stdlib.ProcessorFormatter.wrap_for_formatter,
        ],
        context_class=dict,
        logger_factory=structlog.stdlib.LoggerFactory(),
        cache_logger_on_first_use=True,
    )

    return structlog.get_logger()


# 全局 logger 实例
logger = setup_logger()


def log_api_call(
    module: str, method: str, params: dict[str, Any] | None = None, duration: float | None = None
) -> None:
    """记录 API 调用日志"""
    logger.info(
        "api_call",
        module=module,
        method=method,
        params=params or {},
        duration_ms=int(duration * 1000) if duration else None,
    )


def log_error(
    module: str, method: str, error: Exception, params: dict[str, Any] | None = None
) -> None:
    """记录错误日志"""
    logger.error(
        "api_error",
        module=module,
        method=method,
        params=params or {},
        error_type=type(error).__name__,
        error_message=str(error),
        exc_info=True,
    )
