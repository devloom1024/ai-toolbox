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
            "stock_zh_a_hist",
            symbol=symbol,
            period=period,
            start_date=start_date,
            end_date=end_date,
        )

    # ========== 搜索功能 ==========

    def search_stock(
        self, keyword: str, market: str | None = None, limit: int = 20, offset: int = 0
    ) -> pd.DataFrame:
        """
        搜索股票 - 使用轻量级 API

        Args:
            keyword: 搜索关键字（股票代码或名称）
            market: 市场类型 (CN=A股, HK=港股, US=美股, ETF=ETF, FUND=基金, None=所有市场)
            limit: 返回结果数量限制
            offset: 分页偏移量

        Returns:
            匹配的股票列表 (包含代码、名称、市场、类型)
        """
        # 根据市场类型获取数据
        if market == "CN":
            all_stocks = self._call_akshare("stock_info_a_code_name")
            all_stocks = all_stocks.rename(columns={"code": "代码", "name": "名称"})
            all_stocks["市场"] = "CN"
            all_stocks["类型"] = "STOCK"
        elif market == "HK":
            all_stocks = self._call_akshare("stock_hk_spot_em")
            all_stocks["市场"] = "HK"
            all_stocks["类型"] = "STOCK"
        elif market == "US":
            all_stocks = self._call_akshare("stock_us_spot_em")
            all_stocks["市场"] = "US"
            all_stocks["类型"] = "STOCK"
        elif market == "ETF":
            all_stocks = self._call_akshare("fund_etf_spot_em")
            all_stocks["市场"] = "CN"
            all_stocks["类型"] = "ETF"
        elif market == "FUND":
            all_stocks = self._call_akshare("fund_etf_spot_em")
            all_stocks["市场"] = "CN"
            all_stocks["类型"] = "FUND"
        else:
            # 搜索所有市场 - 合并 A 股、港股、美股、ETF 数据
            stocks_list = []
            try:
                cn_stocks = self._call_akshare("stock_info_a_code_name")
                cn_stocks = cn_stocks.rename(columns={"code": "代码", "name": "名称"})
                cn_stocks["市场"] = "CN"
                cn_stocks["类型"] = "STOCK"
                stocks_list.append(cn_stocks)
            except Exception as e:
                log_error("akshare", "stock_info_a_code_name", e)
            try:
                hk_stocks = self._call_akshare("stock_hk_spot_em")
                hk_stocks["市场"] = "HK"
                hk_stocks["类型"] = "STOCK"
                stocks_list.append(hk_stocks)
            except Exception as e:
                log_error("akshare", "stock_hk_spot_em", e)
            try:
                us_stocks = self._call_akshare("stock_us_spot_em")
                us_stocks["市场"] = "US"
                us_stocks["类型"] = "STOCK"
                stocks_list.append(us_stocks)
            except Exception as e:
                log_error("akshare", "stock_us_spot_em", e)
            try:
                etf_stocks = self._call_akshare("fund_etf_spot_em")
                etf_stocks["市场"] = "CN"
                etf_stocks["类型"] = "ETF"
                stocks_list.append(etf_stocks)
            except Exception as e:
                log_error("akshare", "fund_etf_spot_em", e)

            if not stocks_list:
                return pd.DataFrame(columns=["代码", "名称", "市场", "类型"])

            # 合并数据，去除重复
            all_stocks = pd.concat(stocks_list, ignore_index=True)
            all_stocks = all_stocks.drop_duplicates(subset=["代码"], keep="first")

        # 确保包含必需的列
        required_columns = ["代码", "名称", "市场", "类型"]
        for col in required_columns:
            if col not in all_stocks.columns:
                all_stocks[col] = None

        # 只保留必需的列
        all_stocks = all_stocks[required_columns]

        if not keyword:
            # 应用分页
            return all_stocks.iloc[offset : offset + limit]

        # 按代码或名称匹配
        mask = (
            all_stocks["代码"].astype(str).str.contains(keyword, case=False, na=False)
            | all_stocks["名称"].astype(str).str.contains(keyword, case=False, na=False)
        )

        filtered = all_stocks[mask]
        # 应用分页
        return filtered.iloc[offset : offset + limit]

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
