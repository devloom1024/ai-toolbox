"""
Python Services Gateway

统一接入 akshare, yfinance, tushare 等 Python 库的网关服务
"""
from .app import create_app

__version__ = "1.0.0"
__all__ = ["create_app"]
