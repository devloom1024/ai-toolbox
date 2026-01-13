"""
中间件配置
"""
import time
from typing import Callable

from fastapi import FastAPI, Request
from starlette.middleware.base import BaseHTTPMiddleware

from ..utils.logger import logger


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    """请求日志中间件"""

    async def dispatch(self, request: Request, call_next: Callable):
        """处理请求并记录日志"""
        start_time = time.time()

        # 记录请求信息
        logger.info(
            "request_start",
            method=request.method,
            path=request.url.path,
            client=request.client.host if request.client else None,
        )

        # 处理请求
        try:
            response = await call_next(request)
            duration = time.time() - start_time

            # 记录响应信息
            logger.info(
                "request_complete",
                method=request.method,
                path=request.url.path,
                status_code=response.status_code,
                duration_ms=int(duration * 1000),
            )

            return response

        except Exception as e:
            duration = time.time() - start_time
            logger.error(
                "request_error",
                method=request.method,
                path=request.url.path,
                error=str(e),
                duration_ms=int(duration * 1000),
                exc_info=True,
            )
            raise


def setup_middleware(app: FastAPI) -> None:
    """配置所有中间件"""
    app.add_middleware(RequestLoggingMiddleware)
