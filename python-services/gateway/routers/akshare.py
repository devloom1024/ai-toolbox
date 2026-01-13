"""
Akshare API 路由
"""
import traceback

from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel

from ..modules import registry
from ..modules.akshare.mapper import AkshareMapper
from ..modules.akshare.module import AkshareModule
from ..utils.logger import logger

router = APIRouter()


class QuoteResponse(BaseModel):
    """行情响应"""

    code: int = 0
    message: str = "success"
    data: list[dict] | dict | None = None


def get_akshare_module() -> AkshareModule:
    """获取 Akshare 模块"""
    try:
        module = registry.get("akshare")
        if not isinstance(module, AkshareModule):
            raise TypeError("Invalid module type")
        return module
    except KeyError:
        raise HTTPException(status_code=503, detail="Akshare module not available")


@router.get("/quote/realtime", response_model=QuoteResponse)
async def get_realtime_quote(symbol: str = Query(..., description="股票代码")):
    """
    获取实时行情

    Args:
        symbol: 股票代码（如 "600000"）
    """
    try:
        module = get_akshare_module()
        df = module.client.get_realtime_quote(symbol)
        data = AkshareMapper.map_quote_data(df)
        return QuoteResponse(data=data)
    except Exception as e:
        logger.error("API error", method="get_realtime_quote", error=str(e), traceback=traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/quote/kline", response_model=QuoteResponse)
async def get_kline(
    symbol: str = Query(..., description="股票代码"),
    period: str = Query("daily", description="周期 daily/weekly/monthly"),
    start_date: str = Query("", description="开始日期 YYYYMMDD"),
    end_date: str = Query("", description="结束日期 YYYYMMDD"),
):
    """
    获取 K 线数据

    Args:
        symbol: 股票代码
        period: 周期
        start_date: 开始日期
        end_date: 结束日期
    """
    try:
        module = get_akshare_module()
        df = module.client.get_kline(symbol, period, start_date, end_date)
        data = AkshareMapper.map_kline_data(df)
        return QuoteResponse(data=data)
    except Exception as e:
        logger.error("API error", method="get_kline", error=str(e), traceback=traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/search", response_model=QuoteResponse)
async def search_stock(
    keyword: str = Query(..., description="搜索关键字（股票代码或名称）"),
    market: str | None = Query(None, description="市场类型: CN=A股, HK=港股, US=美股, ETF=ETF, FUND=基金"),
    limit: int = Query(20, ge=1, le=100, description="返回结果数量限制"),
):
    """
    搜索股票

    Args:
        keyword: 股票代码或名称关键字
        market: 市场类型（可选，不传搜索所有市场）
        limit: 返回结果数量
    """
    try:
        module = get_akshare_module()
        df = module.client.search_stock(keyword, market, limit)
        data = AkshareMapper.map_search_results(df)
        return QuoteResponse(data=data)
    except Exception as e:
        logger.error("API error", method="search_stock", keyword=keyword, market=market, error=str(e), traceback=traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/fundamental/company", response_model=QuoteResponse)
async def get_company_info(symbol: str = Query(..., description="股票代码")):
    """
    获取公司基本信息

    Args:
        symbol: 股票代码
    """
    try:
        module = get_akshare_module()
        df = module.client.get_company_info(symbol)
        data = AkshareMapper.map_financial_data(df)
        return QuoteResponse(data=data)
    except Exception as e:
        logger.error("API error", method="get_company_info", error=str(e), traceback=traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/fundamental/financial", response_model=QuoteResponse)
async def get_financial_indicators(symbol: str = Query(..., description="股票代码")):
    """
    获取财务指标

    Args:
        symbol: 股票代码
    """
    try:
        module = get_akshare_module()
        df = module.client.get_financial_indicators(symbol)
        data = AkshareMapper.map_financial_data(df)
        return QuoteResponse(data=data)
    except Exception as e:
        logger.error("API error", method="get_financial_indicators", error=str(e), traceback=traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/capital/flow", response_model=QuoteResponse)
async def get_capital_flow(symbol: str = Query(..., description="股票代码")):
    """
    获取资金流向

    Args:
        symbol: 股票代码
    """
    try:
        module = get_akshare_module()
        df = module.client.get_capital_flow(symbol)
        data = AkshareMapper.map_capital_flow(df)
        return QuoteResponse(data=data)
    except Exception as e:
        logger.error("API error", method="get_capital_flow", error=str(e), traceback=traceback.format_exc())
        raise HTTPException(status_code=500, detail=str(e))
