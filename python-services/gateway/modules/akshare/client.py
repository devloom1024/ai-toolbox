"""
Akshare 客户端封装
"""
from typing import Any

import pandas as pd

from ...config import settings
from ...utils.logger import log_api_call, log_error


class AkshareClient:
    """Akshare 客户端 - 封装 akshare 库调用"""

    def __init__(self):
        """初始化客户端"""
        self._akshare = None
        self._timeout = settings.timeout_akshare

    async def initialize(self) -> None:
        """初始化 akshare 库"""
        try:
            import akshare as ak

            self._akshare = ak
        except ImportError as e:
            raise RuntimeError("akshare library not installed") from e

    def _call_akshare(self, method_name: str, **kwargs: Any) -> pd.DataFrame:
        """
        调用 akshare 方法

        Args:
            method_name: akshare 方法名
            **kwargs: 方法参数

        Returns:
            DataFrame 结果
        """
        if not self._akshare:
            raise RuntimeError("Akshare client not initialized")

        method = getattr(self._akshare, method_name, None)
        if not method:
            raise ValueError(f"Method '{method_name}' not found in akshare")

        try:
            result = method(**kwargs)
            log_api_call("akshare", method_name, kwargs)
            return result
        except Exception as e:
            log_error("akshare", method_name, e, kwargs)
            raise

    # ========== 行情数据 ==========

    def get_realtime_quote(self, symbol: str) -> pd.DataFrame:
        """
        获取实时行情

        Args:
            symbol: 股票代码（如 "sh600000"）

        Returns:
            实时行情数据
        """
        return self._call_akshare("stock_zh_a_spot_em")

    def get_kline(
        self, symbol: str, period: str = "daily", start_date: str = "", end_date: str = ""
    ) -> pd.DataFrame:
        """
        获取 K 线数据

        Args:
            symbol: 股票代码
            period: 周期 (daily/weekly/monthly)
            start_date: 开始日期 YYYYMMDD
            end_date: 结束日期 YYYYMMDD

        Returns:
            K 线数据
        """
        return self._call_akshare(
            "stock_zh_a_hist", symbol=symbol, period=period, start_date=start_date, end_date=end_date
        )

    # ========== 搜索功能 ==========

    def search_stock(self, keyword: str) -> pd.DataFrame:
        """
        搜索股票

        Args:
            keyword: 搜索关键字（股票代码或名称）

        Returns:
            匹配的股票列表
        """
        # 获取所有 A 股列表
        all_stocks = self._call_akshare("stock_zh_a_spot_em")

        # 过滤匹配的股票
        if not keyword:
            return all_stocks.head(20)  # 返回前 20 个

        # 按代码或名称匹配
        mask = (
            all_stocks["代码"].str.contains(keyword, case=False, na=False)
            | all_stocks["名称"].str.contains(keyword, case=False, na=False)
        )

        return all_stocks[mask].head(50)  # 最多返回 50 条

    # ========== 基本面数据 ==========

    def get_company_info(self, symbol: str) -> pd.DataFrame:
        """
        获取公司基本信息

        Args:
            symbol: 股票代码

        Returns:
            公司信息
        """
        return self._call_akshare("stock_individual_info_em", symbol=symbol)

    def get_financial_indicators(self, symbol: str) -> pd.DataFrame:
        """
        获取财务指标

        Args:
            symbol: 股票代码

        Returns:
            财务指标数据
        """
        return self._call_akshare("stock_financial_analysis_indicator", symbol=symbol)

    # ========== 资金流向 ==========

    def get_capital_flow(self, symbol: str) -> pd.DataFrame:
        """
        获取资金流向

        Args:
            symbol: 股票代码

        Returns:
            资金流向数据
        """
        return self._call_akshare("stock_individual_fund_flow_rank", symbol=symbol)

    # ========== 市场指数 ==========

    def get_index_quote(self, symbol: str) -> pd.DataFrame:
        """
        获取指数行情

        Args:
            symbol: 指数代码（如 "sh000001" 上证指数）

        Returns:
            指数行情数据
        """
        return self._call_akshare("stock_zh_index_spot_em", symbol=symbol)
