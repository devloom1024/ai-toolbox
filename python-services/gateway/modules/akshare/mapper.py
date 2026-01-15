"""
数据映射器 - DataFrame 到标准格式

根据标的类型（股票/指数/ETF）进行差异化字段映射，
对齐 Java DTO 格式。
"""

from datetime import datetime
from typing import Any

import pandas as pd


class AkshareMapper:
    """Akshare 数据映射器"""

    @staticmethod
    def dataframe_to_dict(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        将 DataFrame 转换为字典列表

        Args:
            df: pandas DataFrame

        Returns:
            字典列表
        """
        if df is None or df.empty:
            return []

        # 替换 NaN 为 None
        df = df.where(pd.notna(df), None)

        return df.to_dict(orient="records")

    @staticmethod
    def map_kline_data(df: pd.DataFrame, symbol_type: str = "STOCK") -> list[dict[str, Any]]:
        """
        映射 K 线数据 - 差异化处理

        Args:
            df: akshare 返回的 K 线 DataFrame
            symbol_type: 标的类型 (STOCK, INDEX, ETF)

        Returns:
            标准化的 K 线数据列表
        """
        if df is None or df.empty:
            return []

        # 根据标的类型选择字段映射
        if symbol_type == "INDEX":
            # 指数 K 线：基础字段
            column_mapping = {
                "date": "datetime",
                "日期": "datetime",
                "open": "open",
                "开盘": "open",
                "high": "high",
                "最高": "high",
                "low": "low",
                "最低": "low",
                "close": "close",
                "收盘": "close",
                "volume": "volume",
                "成交量": "volume",
            }
        else:
            # 股票/ETF K 线：完整字段
            column_mapping = {
                "日期": "datetime",
                "开盘": "open",
                "收盘": "close",
                "最高": "high",
                "最低": "low",
                "成交量": "volume",
                "成交额": "turnover",
                "涨跌幅": "changePercent",
                "振幅": "amplitude",
            }

        df_renamed = df.rename(columns=column_mapping)

        # 确保 datetime 字段格式正确
        if "datetime" in df_renamed.columns:
            df_renamed["datetime"] = pd.to_datetime(df_renamed["datetime"]).dt.strftime(
                "%Y-%m-%d %H:%M:%S"
            )

        return AkshareMapper.dataframe_to_dict(df_renamed)

    @staticmethod
    def map_fundamental_data(raw_data: dict[str, Any]) -> dict[str, Any]:
        """
        映射基本面数据 - 差异化处理

        Args:
            raw_data: client 返回的原始数据字典

        Returns:
            标准化的基本面数据
        """
        symbol_type = raw_data.get("type", "STOCK")
        df = raw_data.get("raw_data")

        if symbol_type == "INDEX":
            # 指数没有基本面数据
            return {
                "symbol": None,
                "type": symbol_type,
                "lastUpdated": datetime.utcnow().isoformat() + "Z",
            }

        if df is None or df.empty:
            result = {
                "type": symbol_type,
                "lastUpdated": datetime.utcnow().isoformat() + "Z",
            }
            if symbol_type == "ETF":
                result["error"] = raw_data.get("error")
            return result

        # 替换 NaN 为 None
        df = df.where(pd.notna(df), None)

        if symbol_type == "STOCK":
            # 股票：解析 stock_individual_info_em 返回的数据
            # 数据格式为两列：item, value
            item_value_dict = dict(zip(df["item"].astype(str), df["value"]))

            result = {
                "symbol": item_value_dict.get("股票代码"),
                "type": symbol_type,
                "marketCap": item_value_dict.get("总市值"),
                "circulatingMarketCap": item_value_dict.get("流通市值"),
                "totalShares": item_value_dict.get("总股本"),
                "circulatingShares": item_value_dict.get("流通股"),
                "peRatio": item_value_dict.get("市盈率"),
                "pbRatio": item_value_dict.get("市净率"),
                "dividendYield": item_value_dict.get("股息率"),
                "industry": item_value_dict.get("行业"),
                "listedDate": item_value_dict.get("上市时间"),
                "lastUpdated": datetime.utcnow().isoformat() + "Z",
            }

            # 转换为数值类型
            for key in [
                "marketCap",
                "circulatingMarketCap",
                "totalShares",
                "circulatingShares",
                "peRatio",
                "pbRatio",
                "dividendYield",
            ]:
                if result.get(key) is not None:
                    try:
                        result[key] = float(result[key])
                    except (ValueError, TypeError):
                        result[key] = None

            return result

        elif symbol_type == "ETF":
            # ETF：解析 fund_etf_spot_em 返回的数据
            if len(df) > 0:
                row = df.iloc[0]
                result = {
                    "symbol": row.get("代码"),
                    "type": symbol_type,
                    "totalAssets": row.get("规模"),
                    "nav": row.get("净值"),
                    "navDate": row.get("日期"),
                    "lastUpdated": datetime.utcnow().isoformat() + "Z",
                }

                # 转换为数值类型
                if result.get("totalAssets") is not None:
                    try:
                        result["totalAssets"] = float(result["totalAssets"])
                    except (ValueError, TypeError):
                        result["totalAssets"] = None

                if result.get("nav") is not None:
                    try:
                        result["nav"] = float(result["nav"])
                    except (ValueError, TypeError):
                        result["nav"] = None

                return result

            return {
                "type": symbol_type,
                "error": raw_data.get("error"),
                "lastUpdated": datetime.utcnow().isoformat() + "Z",
            }

        return {
            "type": symbol_type,
            "lastUpdated": datetime.utcnow().isoformat() + "Z",
        }

    @staticmethod
    def map_financial_data(raw_data: dict[str, Any]) -> dict[str, Any]:
        """
        映射财务指标数据 - 差异化处理

        Args:
            raw_data: client 返回的原始数据字典

        Returns:
            标准化的财务指标数据
        """
        symbol_type = raw_data.get("type", "STOCK")
        indicator = raw_data.get("indicator", "report")
        df = raw_data.get("raw_data", pd.DataFrame())

        if symbol_type in ("INDEX", "ETF"):
            return {
                "symbol": None,
                "type": symbol_type,
                "indicator": indicator,
                "reportDates": [],
                "indicators": [],
            }

        if df is None or df.empty:
            return {
                "symbol": None,
                "type": symbol_type,
                "indicator": indicator,
                "reportDates": [],
                "indicators": [],
            }

        # 替换 NaN 为 None
        df = df.where(pd.notna(df), None)

        # 获取报告期列表
        report_dates = df["REPORT_DATE"].tolist() if "REPORT_DATE" in df.columns else []

        # 标准化字段映射
        column_mapping = {
            "REPORT_DATE": "reportDate",
            "REPORT_DATE_NAME": "reportDateName",
            "TOTALOPERATEREVE": "revenue",
            "PARENTNETPROFIT": "netProfit",
            "MLR": "grossProfit",
            "KCFJCXSYJLR": "nonRecurringNetProfit",
            "TOTALOPERATEREVETZ": "revenueGrowth",
            "PARENTNETPROFITTZ": "netProfitGrowth",
            "ROEJQ": "roe",
            "ROEKCJQ": "roeDiluted",
            "XSMLL": "grossMargin",
            "XSJLL": "netMargin",
            "EPSJB": "basicEps",
            "EPSXS": "dilutedEps",
            "BPS": "bvps",
            "ZZCJLL": "totalAssetsReturn",
            "TOTAL_ASSETS": "totalAssets",
            "TOTAL_LIABILITIES": "totalLiabilities",
            "SHAREHOLDERS_EQUITY": "shareholdersEquity",
        }

        df_renamed = df.rename(columns=column_mapping)

        # 提取报告期和指标数据
        indicators = []
        for _, row in df_renamed.iterrows():
            indicator = {
                "reportDate": row.get("reportDate"),
                "revenue": row.get("revenue"),
                "netProfit": row.get("netProfit"),
                "operatingProfit": row.get("grossProfit"),
                "totalAssets": row.get("totalAssets"),
                "totalLiabilities": row.get("totalLiabilities"),
                "shareholdersEquity": row.get("shareholdersEquity"),
                "roe": row.get("roe"),
                "roeDiluted": row.get("roeDiluted"),
                "grossMargin": row.get("grossMargin"),
                "netMargin": row.get("netMargin"),
                "basicEps": row.get("basicEps"),
                "dilutedEps": row.get("dilutedEps"),
                "bvps": row.get("bvps"),
            }
            indicators.append(indicator)

        return {
            "symbol": df["SECURITY_CODE"].iloc[0] if "SECURITY_CODE" in df.columns else None,
            "type": symbol_type,
            "indicator": indicator,
            "reportDates": report_dates,
            "indicators": indicators,
        }

    @staticmethod
    def map_capital_flow_data(raw_data: dict[str, Any]) -> dict[str, Any]:
        """
        映射资金流向数据 - 差异化处理

        Args:
            raw_data: client 返回的原始数据字典

        Returns:
            标准化的资金流向数据
        """
        symbol_type = raw_data.get("type", "STOCK")
        error = raw_data.get("error")
        df = raw_data.get("raw_data")

        # 指数不支持
        if symbol_type == "INDEX":
            return {
                "symbol": None,
                "type": symbol_type,
                "error": error,
                "lastUpdated": datetime.utcnow().isoformat() + "Z",
            }

        # ETF 可能不支持
        if symbol_type == "ETF":
            return {
                "symbol": None,
                "type": symbol_type,
                "error": error,
                "lastUpdated": datetime.utcnow().isoformat() + "Z",
            }

        # 股票资金流向
        if df is None or df.empty:
            return {
                "symbol": None,
                "type": symbol_type,
                "error": error,
                "lastUpdated": datetime.utcnow().isoformat() + "Z",
            }

        # 替换 NaN 为 None
        df = df.where(pd.notna(df), None)

        # 标准化字段映射
        column_mapping = {
            "日期": "date",
            "收盘价": "closePrice",
            "涨跌幅": "changePercent",
            "主力净流入-净额": "mainNetInflow",
            "主力净流入-净占比": "mainNetInflowRate",
            "超大单净流入-净额": "superNetInflow",
            "超大单净流入-净占比": "superNetInflowRate",
            "大单净流入-净额": "largeNetInflow",
            "大单净流入-净占比": "largeNetInflowRate",
            "中单净流入-净额": "mediumNetInflow",
            "中单净流入-净占比": "mediumNetInflowRate",
            "小单净流入-净额": "smallNetInflow",
            "小单净流入-净占比": "smallNetInflowRate",
        }

        df_renamed = df.rename(columns=column_mapping)

        # 返回最新一条数据
        if len(df_renamed) > 0:
            latest = df_renamed.iloc[-1]
            return {
                "symbol": None,
                "type": symbol_type,
                "mainNetInflow": latest.get("mainNetInflow"),
                "mainNetInflowRate": latest.get("mainNetInflowRate"),
                "superNetInflow": latest.get("superNetInflow"),
                "superNetInflowRate": latest.get("superNetInflowRate"),
                "largeNetInflow": latest.get("largeNetInflow"),
                "largeNetInflowRate": latest.get("largeNetInflowRate"),
                "mediumNetInflow": latest.get("mediumNetInflow"),
                "mediumNetInflowRate": latest.get("mediumNetInflowRate"),
                "smallNetInflow": latest.get("smallNetInflow"),
                "smallNetInflowRate": latest.get("smallNetInflowRate"),
                "lastUpdated": datetime.utcnow().isoformat() + "Z",
            }

        return {
            "symbol": None,
            "type": symbol_type,
            "lastUpdated": datetime.utcnow().isoformat() + "Z",
        }

    @staticmethod
    def map_search_results(df: pd.DataFrame) -> list[dict[str, Any]]:
        """
        映射股票搜索结果 - 返回 symbol, name, market, type

        Args:
            df: akshare 返回的搜索结果 DataFrame

        Returns:
            标准化的搜索结果列表
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

        result_df = df_renamed[available_fields]

        return AkshareMapper.dataframe_to_dict(result_df)
