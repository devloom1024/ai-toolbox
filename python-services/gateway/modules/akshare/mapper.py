"""
数据映射器 - DataFrame 到标准格式
"""
from typing import Any

import pandas as pd


class AkshareMapper:
    """Akshare 数据映射器"""

    @staticmethod
    def dataframe_to_dict(df: pd.DataFrame, orient: str = "records") -> list[dict[str, Any]] | dict:
        """
        将 DataFrame 转换为字典

        Args:
            df: pandas DataFrame
            orient: 转换方向 (records/dict/list 等)

        Returns:
            字典或列表
        """
        if df is None or df.empty:
            return [] if orient == "records" else {}

        # 替换 NaN 为 None
        df = df.where(pd.notna(df), None)

        return df.to_dict(orient=orient)

    @staticmethod
    def map_quote_data(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        映射实时行情数据 - 匹配 Java QuoteResponse 字段

        Args:
            df: akshare 返回的行情 DataFrame

        Returns:
            标准化的行情数据列表
        """
        if df is None or df.empty:
            return []

        # 标准化字段名（akshare 使用中文列名，映射到 Java QuoteResponse 字段）
        column_mapping = {
            "代码": "symbol",
            "名称": "name",
            "最新价": "currentPrice",
            "今开": "openPrice",
            "最高": "highPrice",
            "最低": "lowPrice",
            "昨收": "preClosePrice",
            "涨跌额": "change",
            "涨跌幅": "changePercent",
            "成交量": "volume",
            "成交额": "turnover",
            "换手率": "turnoverRate",
            "振幅": "amplitude",
            "总市值": "marketCap",
            "流通市值": "circulatingMarketCap",
            "市盈率": "peRatio",
            "市净率": "pbRatio",
            "股息率": "dividendYield",
        }

        df_renamed = df.rename(columns=column_mapping)
        return AkshareMapper.dataframe_to_dict(df_renamed)

    @staticmethod
    def map_kline_data(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        映射 K 线数据 - 匹配 Java KlineResponse.KlineItem 字段

        Args:
            df: akshare 返回的 K 线 DataFrame

        Returns:
            标准化的 K 线数据列表
        """
        if df is None or df.empty:
            return []

        column_mapping = {
            "日期": "datetime",
            "开盘": "open",
            "收盘": "close",
            "最高": "high",
            "最低": "low",
            "成交量": "volume",
            "成交额": "turnover",
            "涨跌幅": "changePercent",
        }

        df_renamed = df.rename(columns=column_mapping)

        # 确保 datetime 字段格式正确（添加时间部分）
        if "datetime" in df_renamed.columns:
            df_renamed["datetime"] = pd.to_datetime(df_renamed["datetime"]).dt.strftime(
                "%Y-%m-%d %H:%M:%S"
            )

        return AkshareMapper.dataframe_to_dict(df_renamed)

    @staticmethod
    def map_search_results(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        映射股票搜索结果 - 返回 symbol, name, market, type

        Args:
            df: akshare 返回的搜索结果 DataFrame

        Returns:
            标准化的搜索结果列表 (包含 symbol, name, market, type)
        """
        if df is None or df.empty:
            return []

        column_mapping = {
            "代码": "symbol",
            "名称": "name",
            "市场": "market",
            "类型": "type",
        }

        df_renamed = df.rename(columns=column_mapping)

        # 保留所有必需字段
        required_fields = ["symbol", "name", "market", "type"]
        available_fields = [f for f in required_fields if f in df_renamed.columns]

        return AkshareMapper.dataframe_to_dict(df_renamed[available_fields])

    @staticmethod
    def map_financial_data(df: pd.DataFrame) -> dict[str, Any]:
        """
        映射财务数据 - 匹配 Java FundamentalResponse 或 FinancialResponse 字段

        Args:
            df: akshare 返回的财务数据 DataFrame

        Returns:
            标准化的财务数据（单条记录或字典）
        """
        if df is None or df.empty:
            return {}

        # 如果是单行数据（公司基本信息），返回字典
        if len(df) == 1:
            # 替换 NaN 为 None
            df = df.where(pd.notna(df), None)
            return df.to_dict(orient="records")[0]

        # 如果是多行数据（财务指标历史），返回列表
        return AkshareMapper.dataframe_to_dict(df)

    @staticmethod
    def map_capital_flow(df: pd.DataFrame) -> dict[str, Any]:
        """
        映射资金流向数据 - 匹配 Java CapitalFlowResponse 字段

        Args:
            df: akshare 返回的资金流向 DataFrame

        Returns:
            标准化的资金流向数据（单条记录）
        """
        if df is None or df.empty:
            return {}

        column_mapping = {
            "日期": "date",
            "主力净流入": "mainNetInflow",
            "主力净流入占比": "mainNetInflowRate",
            "超大单净流入": "superNetInflow",
            "大单净流入": "largeNetInflow",
            "中单净流入": "mediumNetInflow",
            "小单净流入": "smallNetInflow",
        }

        df_renamed = df.rename(columns=column_mapping)

        # 如果是单行数据，返回字典
        if len(df_renamed) == 1:
            df_renamed = df_renamed.where(pd.notna(df_renamed), None)
            return df_renamed.to_dict(orient="records")[0]

        # 如果是多行数据，返回最新一条
        if len(df_renamed) > 0:
            df_renamed = df_renamed.where(pd.notna(df_renamed), None)
            return df_renamed.iloc[-1].to_dict()

        return {}
