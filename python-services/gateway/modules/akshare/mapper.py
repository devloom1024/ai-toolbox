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
        映射实时行情数据

        Args:
            df: akshare 返回的行情 DataFrame

        Returns:
            标准化的行情数据列表
        """
        if df is None or df.empty:
            return []

        # 标准化字段名（akshare 使用中文列名）
        column_mapping = {
            "代码": "symbol",
            "名称": "name",
            "最新价": "price",
            "涨跌幅": "change_pct",
            "涨跌额": "change",
            "成交量": "volume",
            "成交额": "amount",
            "振幅": "amplitude",
            "最高": "high",
            "最低": "low",
            "今开": "open",
            "昨收": "pre_close",
            "换手率": "turnover_rate",
            "市盈率": "pe_ratio",
            "市净率": "pb_ratio",
        }

        df_renamed = df.rename(columns=column_mapping)
        return AkshareMapper.dataframe_to_dict(df_renamed)

    @staticmethod
    def map_kline_data(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        映射 K 线数据

        Args:
            df: akshare 返回的 K 线 DataFrame

        Returns:
            标准化的 K 线数据列表
        """
        if df is None or df.empty:
            return []

        column_mapping = {
            "日期": "date",
            "开盘": "open",
            "收盘": "close",
            "最高": "high",
            "最低": "low",
            "成交量": "volume",
            "成交额": "amount",
            "振幅": "amplitude",
            "涨跌幅": "change_pct",
            "涨跌额": "change",
            "换手率": "turnover_rate",
        }

        df_renamed = df.rename(columns=column_mapping)
        return AkshareMapper.dataframe_to_dict(df_renamed)

    @staticmethod
    def map_search_results(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        映射股票搜索结果 - 仅返回 symbol 和 name

        Args:
            df: akshare 返回的搜索结果 DataFrame

        Returns:
            标准化的搜索结果列表 (仅包含 symbol 和 name)
        """
        if df is None or df.empty:
            return []

        column_mapping = {
            "代码": "symbol",
            "名称": "name",
        }

        df_renamed = df.rename(columns=column_mapping)

        # 只保留 symbol 和 name 字段
        required_fields = ["symbol", "name"]
        available_fields = [f for f in required_fields if f in df_renamed.columns]

        return AkshareMapper.dataframe_to_dict(df_renamed[available_fields])

    @staticmethod
    def map_financial_data(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        映射财务数据

        Args:
            df: akshare 返回的财务数据 DataFrame

        Returns:
            标准化的财务数据列表
        """
        if df is None or df.empty:
            return []

        # 财务数据字段较多，使用通用映射
        return AkshareMapper.dataframe_to_dict(df)

    @staticmethod
    def map_capital_flow(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        映射资金流向数据

        Args:
            df: akshare 返回的资金流向 DataFrame

        Returns:
            标准化的资金流向数据列表
        """
        if df is None or df.empty:
            return []

        column_mapping = {
            "日期": "date",
            "主力净流入": "main_net_inflow",
            "主力净流入占比": "main_net_inflow_pct",
            "超大单净流入": "super_large_net_inflow",
            "超大单净流入占比": "super_large_net_inflow_pct",
            "大单净流入": "large_net_inflow",
            "大单净流入占比": "large_net_inflow_pct",
            "中单净流入": "medium_net_inflow",
            "中单净流入占比": "medium_net_inflow_pct",
            "小单净流入": "small_net_inflow",
            "小单净流入占比": "small_net_inflow_pct",
        }

        df_renamed = df.rename(columns=column_mapping)
        return AkshareMapper.dataframe_to_dict(df_renamed)
