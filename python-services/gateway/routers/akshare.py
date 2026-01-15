"""
Akshare API 路由 - 差异化数据接口

根据标的类型（股票/指数/ETF）返回差异化数据，
对齐 Java DTO 格式。
"""

import traceback

from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel

from ..modules import registry
from ..modules.akshare.client import AkshareClient
from ..modules.akshare.mapper import AkshareMapper
from ..modules.akshare.module import AkshareModule
from ..utils.logger import logger

router = APIRouter()


class ApiResponse(BaseModel):
    """统一 API 响应"""

    code: int = 0
    message: str = "success"
    data: dict | list | None = None


def get_akshare_module() -> AkshareModule:
    """获取 Akshare 模块"""
    try:
        module = registry.get("akshare")
        if not isinstance(module, AkshareModule):
            raise TypeError("Invalid module type")
        return module
    except KeyError:
        raise HTTPException(status_code=503, detail="Akshare module not available")


# ========== K 线接口 ==========


@router.get("/kline", response_model=ApiResponse)
async def get_kline(
    symbol: str = Query(..., description="标的代码", example="600000"),
    period: str = Query(
        "daily",
        description="K 线周期",
        example="daily",
    ),
    start_date: str = Query("", description="开始日期 (YYYYMMDD)", example="20240101"),
    end_date: str = Query("", description="结束日期 (YYYYMMDD)", example="20241231"),
    limit: int = Query(500, ge=1, le=5000, description="数据条数限制"),
    adjust: str = Query("", description="复权类型: 空=不复权, qfq=前复权, hfq=后复权"),
):
    """
    获取 K 线数据

    - **股票/ETF**：返回完整字段（成交额、涨跌幅、振幅）
    - **指数**：仅返回基础字段（日期、开盘、最高、最低、收盘、成交量）
    """
    try:
        module = get_akshare_module()
        client: AkshareClient = module.client

        # 检测标的类型
        symbol_type = client.detect_symbol_type(symbol)

        # 获取 K 线数据
        df = client.get_kline(symbol, period, start_date, end_date, limit, adjust)

        # 映射数据
        klines = AkshareMapper.map_kline_data(df, symbol_type)

        data = {
            "symbol": symbol,
            "period": period,
            "adjust": adjust,
            "klines": klines,
        }

        return ApiResponse(data=data)
    except Exception as e:
        logger.error(
            "API error",
            method="get_kline",
            symbol=symbol,
            error=str(e),
            traceback=traceback.format_exc(),
        )
        raise HTTPException(status_code=500, detail=str(e))


# ========== 基本面接口 ==========


@router.get("/fundamental", response_model=ApiResponse)
async def get_fundamental(
    symbol: str = Query(..., description="标的代码", example="600000"),
):
    """
    获取基本面数据

    - **股票**：返回完整字段（市值、市盈率、市净率、行业等）
    - **ETF**：返回基础信息（规模、净值等）
    - **指数**：仅返回基本信息
    """
    try:
        module = get_akshare_module()
        client: AkshareClient = module.client

        # 获取基本面数据
        raw_data = client.get_fundamental(symbol)

        # 映射数据
        data = AkshareMapper.map_fundamental_data(raw_data)

        return ApiResponse(data=data)
    except Exception as e:
        logger.error(
            "API error",
            method="get_fundamental",
            symbol=symbol,
            error=str(e),
            traceback=traceback.format_exc(),
        )
        raise HTTPException(status_code=500, detail=str(e))


# ========== 财务指标接口 ==========


@router.get("/financial", response_model=ApiResponse)
async def get_financial(
    symbol: str = Query(..., description="标的代码", example="600000"),
    indicator: str = Query("report", description="数据类型: report=按报告期, quarter=按单季度"),
):
    """
    获取财务指标数据

    - **股票**：返回 140+ 财务指标（营业收入、净利润、ROE、毛利率等）
    - **ETF/指数**：返回空列表
    """
    try:
        module = get_akshare_module()
        client: AkshareClient = module.client

        # 获取财务指标数据
        raw_data = client.get_financial(symbol, indicator)

        # 映射数据
        data = AkshareMapper.map_financial_data(raw_data)

        return ApiResponse(data=data)
    except Exception as e:
        logger.error(
            "API error",
            method="get_financial",
            symbol=symbol,
            error=str(e),
            traceback=traceback.format_exc(),
        )
        raise HTTPException(status_code=500, detail=str(e))


# ========== 资金流向接口 ==========


@router.get("/capital-flow", response_model=ApiResponse)
async def get_capital_flow(
    symbol: str = Query(..., description="标的代码", example="600000"),
):
    """
    获取资金流向数据

    - **股票**：返回主力/超大单/大单/中单/小单净流入及占比
    - **ETF**：可能不支持
    - **指数**：不支持
    """
    try:
        module = get_akshare_module()
        client: AkshareClient = module.client

        # 获取资金流向数据
        raw_data = client.get_capital_flow(symbol)

        # 映射数据
        data = AkshareMapper.map_capital_flow_data(raw_data)

        return ApiResponse(data=data)
    except Exception as e:
        logger.error(
            "API error",
            method="get_capital_flow",
            symbol=symbol,
            error=str(e),
            traceback=traceback.format_exc(),
        )
        raise HTTPException(status_code=500, detail=str(e))


# ========== 搜索接口（保留现有接口） ==========


@router.get("/search", response_model=ApiResponse)
async def search_stock(
    keyword: str = Query(..., description="搜索关键字（股票代码或名称）", example="茅台"),
    market: str | None = Query(
        None, description="市场类型: A_SHARE=A股, HK=港股, US=美股, ETF=ETF, FUND=基金"
    ),
    limit: int = Query(20, ge=1, le=100, description="返回结果数量限制"),
    offset: int = Query(0, ge=0, description="分页偏移量"),
):
    """
    搜索股票/ETF 等标的

    保留现有接口，不做修改。
    """
    try:
        module = get_akshare_module()
        df = module.client.search_stock(keyword, market, limit, offset)
        data = AkshareMapper.map_search_results(df)
        return ApiResponse(data=data)
    except Exception as e:
        logger.error(
            "API error",
            method="search_stock",
            keyword=keyword,
            market=market,
            error=str(e),
            traceback=traceback.format_exc(),
        )
        raise HTTPException(status_code=500, detail=str(e))


# ========== 实时行情接口（保留） ==========


@router.get("/quote/realtime", response_model=ApiResponse)
async def get_realtime_quote(symbol: str = Query(..., description="股票代码")):
    """
    获取实时行情
    """
    try:
        module = get_akshare_module()
        df = module.client.get_realtime_quote(symbol)

        # 使用原有的映射方法
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
            "市盈率-动态": "peRatio",
            "市净率": "pbRatio",
        }

        df_renamed = df.rename(columns=column_mapping)
        data = df_renamed.to_dict(orient="records")

        return ApiResponse(data=data)
    except Exception as e:
        logger.error(
            "API error",
            method="get_realtime_quote",
            error=str(e),
            traceback=traceback.format_exc(),
        )
        raise HTTPException(status_code=500, detail=str(e))


# ========== 指数行情接口（保留） ==========


@router.get("/quote/index", response_model=ApiResponse)
async def get_index_quote(symbol: str = Query(..., description="指数代码", example="sh000001")):
    """
    获取指数行情
    """
    try:
        module = get_akshare_module()
        df = module.client.get_index_quote(symbol)

        column_mapping = {
            "代码": "symbol",
            "名称": "name",
            "最新价": "currentPrice",
            "涨跌额": "change",
            "涨跌幅": "changePercent",
            "成交量": "volume",
            "成交额": "turnover",
            "振幅": "amplitude",
            "最高": "highPrice",
            "最低": "lowPrice",
            "今开": "openPrice",
            "昨收": "preClosePrice",
        }

        df_renamed = df.rename(columns=column_mapping)
        data = df_renamed.to_dict(orient="records")

        return ApiResponse(data=data)
    except Exception as e:
        logger.error(
            "API error",
            method="get_index_quote",
            error=str(e),
            traceback=traceback.format_exc(),
        )
        raise HTTPException(status_code=500, detail=str(e))
