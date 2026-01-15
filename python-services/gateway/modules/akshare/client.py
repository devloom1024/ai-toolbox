"""
Akshare 客户端封装 - 差异化数据获取

根据标的类型（股票/指数/ETF）调用不同的 akshare 接口，
返回最完整的数据。
"""

from typing import Any

import pandas as pd

from ...config import settings
from ...utils.logger import log_api_call, log_error


class SymbolType:
    """标的类型常量"""

    STOCK = "STOCK"  # 股票
    INDEX = "INDEX"  # 指数
    ETF = "ETF"  # ETF
    FUND = "FUND"  # 基金


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

    # ========== 标的类型检测 ==========

    def detect_symbol_type(self, symbol: str) -> str:
        """
        检测标的类型

        Args:
            symbol: 标的代码 (支持带市场前缀或不带)

        Returns:
            标的类型: STOCK, INDEX, ETF, FUND
        """
        # 清理 symbol，去掉市场前缀
        clean_symbol = symbol
        prefixes = ["sh", "sz", "bj"]
        for prefix in prefixes:
            if symbol.lower().startswith(prefix):
                clean_symbol = symbol[len(prefix) :]
                break

        # 检测指数 (000xxx, 399xxx)
        if len(clean_symbol) == 6:
            if clean_symbol.startswith("000") or clean_symbol.startswith("399"):
                return SymbolType.INDEX

        # 检测 ETF (51xxx, 15xxx, 16xxx, 159xxx, 510xxx, 511xxx, 512xxx)
        if len(clean_symbol) == 6:
            if clean_symbol.startswith(("51", "15", "16", "159", "510", "511", "512")):
                return SymbolType.ETF
            # 纯数字 5 开头也可能是 ETF
            if clean_symbol.startswith("5"):
                return SymbolType.ETF

        # 检测基金 (16xxx 可能是场外基金)
        if len(clean_symbol) == 6:
            if clean_symbol.startswith("16"):
                return SymbolType.FUND

        # 默认为股票
        return SymbolType.STOCK

    def _get_market_prefix(self, symbol: str) -> tuple[str, str]:
        """
        获取市场前缀和纯代码

        Args:
            symbol: 标的代码

        Returns:
            (市场前缀, 纯代码)
            如 ("sh", "600000") 或 ("sz", "000001")
        """
        lower_symbol = symbol.lower()
        if lower_symbol.startswith("sh"):
            return "sh", symbol[2:]
        elif lower_symbol.startswith("sz"):
            return "sz", symbol[2:]
        elif lower_symbol.startswith("bj"):
            return "bj", symbol[2:]
        else:
            # 根据代码规则推断市场
            code = symbol
            if len(symbol) == 6:
                if symbol.startswith(("0", "3")):
                    return "sz", symbol
                elif symbol.startswith(("6", "8", "9")):
                    return "sh", symbol
            return "", symbol

    # ========== K 线数据 ==========

    def get_kline(
        self,
        symbol: str,
        period: str = "daily",
        start_date: str = "",
        end_date: str = "",
        limit: int = 500,
        adjust: str = "",
    ) -> pd.DataFrame:
        """
        获取 K 线数据 - 统一接口

        Args:
            symbol: 标的代码
            period: 周期 (daily/weekly/monthly/5min/15min/30min/60min)
            start_date: 开始日期 YYYYMMDD
            end_date: 结束日期 YYYYMMDD
            limit: 数据条数限制
            adjust: 复权类型 ("", "qfq", "hfq")

        Returns:
            K 线数据 DataFrame
        """
        symbol_type = self.detect_symbol_type(symbol)

        if symbol_type == SymbolType.INDEX:
            return self._get_index_kline(symbol, period, start_date, end_date, adjust)
        elif symbol_type == SymbolType.ETF:
            return self._get_etf_kline(symbol, period, start_date, end_date, adjust)
        else:
            return self._get_stock_kline(symbol, period, start_date, end_date, adjust)

    def _get_stock_kline(
        self, symbol: str, period: str, start_date: str, end_date: str, adjust: str
    ) -> pd.DataFrame:
        """
        获取股票 K 线数据

        使用 stock_zh_a_hist 接口，返回完整字段
        """
        # 确定代码格式
        market_prefix, code = self._get_market_prefix(symbol)
        if market_prefix and not code.startswith(market_prefix):
            full_code = f"{market_prefix}{code}"
        else:
            full_code = code if len(code) == 6 else symbol

        return self._call_akshare(
            "stock_zh_a_hist",
            symbol=full_code,
            period=period,
            start_date=start_date,
            end_date=end_date,
            adjust=adjust,
        )

    def _get_index_kline(
        self, symbol: str, period: str, start_date: str, end_date: str, adjust: str
    ) -> pd.DataFrame:
        """
        获取指数 K 线数据

        使用 stock_zh_index_daily 接口，返回基础字段
        """
        # 清理代码
        market_prefix, code = self._get_market_prefix(symbol)

        # 指数代码格式
        if market_prefix:
            index_code = f"{market_prefix}{code}"
        else:
            if code.startswith("000") or code.startswith("399"):
                index_code = f"sh{code}"
            else:
                index_code = code

        return self._call_akshare(
            "stock_zh_index_daily",
            symbol=index_code,
        )

    def _get_etf_kline(
        self, symbol: str, period: str, start_date: str, end_date: str, adjust: str
    ) -> pd.DataFrame:
        """
        获取 ETF K 线数据

        使用 stock_zh_a_hist 接口（ETF 也通过股票接口获取）
        """
        # 清理代码
        market_prefix, code = self._get_market_prefix(symbol)

        # ETF 代码通常需要加上市场前缀
        if market_prefix:
            full_code = f"{market_prefix}{code}"
        else:
            # 510xxx -> sh510xxx, 159xxx -> sz159xxx
            if code.startswith("51") or code.startswith("15"):
                full_code = f"sh{code}"
            elif code.startswith("159"):
                full_code = f"sz{code}"
            else:
                full_code = code

        return self._call_akshare(
            "stock_zh_a_hist",
            symbol=full_code,
            period=period,
            start_date=start_date,
            end_date=end_date,
            adjust=adjust,
        )

    # ========== 基本面数据 ==========

    def get_fundamental(self, symbol: str) -> dict[str, Any]:
        """
        获取基本面数据 - 统一接口

        Args:
            symbol: 标的代码

        Returns:
            基本面数据字典
        """
        symbol_type = self.detect_symbol_type(symbol)

        if symbol_type == SymbolType.INDEX:
            return self._get_index_fundamental(symbol)
        elif symbol_type == SymbolType.ETF:
            return self._get_etf_fundamental(symbol)
        else:
            return self._get_stock_fundamental(symbol)

    def _get_stock_fundamental(self, symbol: str) -> dict[str, Any]:
        """
        获取股票基本面数据

        使用 stock_individual_info_em 接口
        """
        # 清理代码
        _, code = self._get_market_prefix(symbol)

        try:
            df = self._call_akshare("stock_individual_info_em", symbol=code)
            return {
                "type": SymbolType.STOCK,
                "raw_data": df,
            }
        except Exception as e:
            log_error("akshare", "get_stock_fundamental", e, {"symbol": symbol})
            return {
                "type": SymbolType.STOCK,
                "raw_data": None,
                "error": str(e),
            }

    def _get_etf_fundamental(self, symbol: str) -> dict[str, Any]:
        """
        获取 ETF 基本面数据

        尝试 fund_etf_spot_em 获取 ETF 信息
        """
        _, code = self._get_market_prefix(symbol)

        try:
            # 尝试 fund_etf_spot_em 获取 ETF 列表信息
            df = self._call_akshare("fund_etf_spot_em")
            # 筛选对应的 ETF
            etf_info = df[df["代码"] == code]
            return {
                "type": SymbolType.ETF,
                "raw_data": etf_info,
            }
        except Exception as e:
            log_error("akshare", "get_etf_fundamental", e, {"symbol": symbol})
            return {
                "type": SymbolType.ETF,
                "raw_data": None,
                "error": str(e),
            }

    def _get_index_fundamental(self, symbol: str) -> dict[str, Any]:
        """
        获取指数基本面数据

        指数没有基本面数据，返回基本信息
        """
        return {
            "type": SymbolType.INDEX,
            "raw_data": None,
            "error": None,
        }

    # ========== 财务指标 ==========

    def get_financial(self, symbol: str, indicator: str = "report") -> dict[str, Any]:
        """
        获取财务指标 - 统一接口

        Args:
            symbol: 标的代码
            indicator: 数据类型 ("report"=按报告期, "quarter"=按单季度)

        Returns:
            财务指标数据字典
        """
        symbol_type = self.detect_symbol_type(symbol)

        if symbol_type in (SymbolType.INDEX, SymbolType.ETF):
            return {
                "type": symbol_type,
                "indicator": indicator,
                "raw_data": pd.DataFrame(),
            }
        else:
            return self._get_stock_financial(symbol, indicator)

    def _get_stock_financial(self, symbol: str, indicator: str) -> dict[str, Any]:
        """
        获取股票财务指标

        使用 stock_financial_analysis_indicator_em 接口（140+ 指标）
        """
        # 清理代码
        _, code = self._get_market_prefix(symbol)

        # 添加市场后缀
        market_prefix, _ = self._get_market_prefix(symbol)
        if market_prefix == "sh":
            full_code = f"{code}.SH"
        elif market_prefix == "sz":
            full_code = f"{code}.SZ"
        else:
            full_code = code

        try:
            df = self._call_akshare(
                "stock_financial_analysis_indicator_em",
                symbol=full_code,
                indicator="按报告期" if indicator == "report" else "按单季度",
            )
            return {
                "type": SymbolType.STOCK,
                "indicator": indicator,
                "raw_data": df,
            }
        except Exception as e:
            log_error("akshare", "get_stock_financial", e, {"symbol": symbol})
            return {
                "type": SymbolType.STOCK,
                "indicator": indicator,
                "raw_data": pd.DataFrame(),
                "error": str(e),
            }

    # ========== 资金流向 ==========

    def get_capital_flow(self, symbol: str) -> dict[str, Any]:
        """
        获取资金流向 - 统一接口

        Args:
            symbol: 标的代码

        Returns:
            资金流向数据字典
        """
        symbol_type = self.detect_symbol_type(symbol)

        if symbol_type == SymbolType.INDEX:
            return {
                "type": SymbolType.INDEX,
                "raw_data": None,
                "error": "INDEX_NOT_SUPPORTED",
            }
        elif symbol_type == SymbolType.ETF:
            return self._get_etf_capital_flow(symbol)
        else:
            return self._get_stock_capital_flow(symbol)

    def _get_stock_capital_flow(self, symbol: str) -> dict[str, Any]:
        """
        获取股票资金流向

        使用 stock_individual_fund_flow 接口
        返回近 100 个交易日的资金流数据
        """
        # 清理代码
        market_prefix, code = self._get_market_prefix(symbol)

        # 确定市场代码
        if market_prefix:
            market = market_prefix[0]  # sh -> sh, sz -> sz
        else:
            # 根据代码规则推断
            if code.startswith(("0", "3")):
                market = "sz"
            else:
                market = "sh"

        try:
            df = self._call_akshare(
                "stock_individual_fund_flow",
                stock=code,
                market=market,
            )
            return {
                "type": SymbolType.STOCK,
                "raw_data": df,
            }
        except Exception as e:
            log_error("akshare", "get_stock_capital_flow", e, {"symbol": symbol})
            return {
                "type": SymbolType.STOCK,
                "raw_data": None,
                "error": str(e),
            }

    def _get_etf_capital_flow(self, symbol: str) -> dict[str, Any]:
        """
        获取 ETF 资金流向

        ETF 可能没有资金流向数据
        """
        return {
            "type": SymbolType.ETF,
            "raw_data": None,
            "error": "ETF_NOT_SUPPORTED",
        }

    # ========== 实时行情（保留） ==========

    def get_realtime_quote(self, symbol: str) -> pd.DataFrame:
        """
        获取实时行情

        Args:
            symbol: 股票代码（如 "sh600000"）

        Returns:
            实时行情数据
        """
        return self._call_akshare("stock_zh_a_spot_em")

    # ========== 市场指数（保留） ==========

    def get_index_quote(self, symbol: str) -> pd.DataFrame:
        """
        获取指数行情

        Args:
            symbol: 指数代码（如 "sh000001" 上证指数）

        Returns:
            指数行情数据
        """
        return self._call_akshare("stock_zh_index_spot_em", symbol=symbol)

    # ========== 搜索功能（保留现有接口） ==========

    def search_stock(
        self, keyword: str, market: str | None = None, limit: int = 20, offset: int = 0
    ) -> pd.DataFrame:
        """
        搜索股票 - 使用轻量级 API

        Args:
            keyword: 搜索关键字（股票代码或名称）
            market: 市场类型 (A_SHARE=A股, HK=港股, US=美股, ETF=ETF, FUND=基金, None=所有市场)
            limit: 返回结果数量限制
            offset: 分页偏移量

        Returns:
            匹配的股票列表 (包含代码、名称、市场、类型)
        """
        # 根据市场类型获取数据
        if market == "A_SHARE":
            all_stocks = self._call_akshare("stock_info_a_code_name")
            all_stocks = all_stocks.rename(columns={"code": "代码", "name": "名称"})
            all_stocks["市场"] = "A_SHARE"
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
            all_stocks["市场"] = "A_SHARE"
            all_stocks["类型"] = "ETF"
        elif market == "FUND":
            all_stocks = self._call_akshare("fund_etf_spot_em")
            all_stocks["市场"] = "A_SHARE"
            all_stocks["类型"] = "FUND"
        else:
            # 搜索所有市场 - 合并 A 股、港股、美股、ETF 数据
            stocks_list = []
            try:
                a_share_stocks = self._call_akshare("stock_info_a_code_name")
                a_share_stocks = a_share_stocks.rename(columns={"code": "代码", "name": "名称"})
                a_share_stocks["市场"] = "A_SHARE"
                a_share_stocks["类型"] = "STOCK"
                stocks_list.append(a_share_stocks)
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
                etf_stocks["市场"] = "A_SHARE"
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
        mask = all_stocks["代码"].astype(str).str.contains(
            keyword, case=False, na=False
        ) | all_stocks["名称"].astype(str).str.contains(keyword, case=False, na=False)

        filtered = all_stocks[mask]
        # 应用分页
        return filtered.iloc[offset : offset + limit]
