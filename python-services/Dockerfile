# ==========================================
# 多阶段构建 - 使用 UV 包管理器
# ==========================================

# ========== 构建阶段 ==========
FROM python:3.11-slim AS builder

WORKDIR /build

# 安装 uv
RUN pip install --no-cache-dir uv

# 复制项目配置文件
COPY pyproject.toml uv.toml ./

# 使用 uv 创建虚拟环境并安装依赖
RUN uv venv /opt/venv && \
    . /opt/venv/bin/activate && \
    uv pip install --no-cache -e ".[akshare,all]"

# ========== 运行阶段 ==========
FROM python:3.11-slim

WORKDIR /app

# 从构建阶段复制虚拟环境
COPY --from=builder /opt/venv /opt/venv

# 复制应用代码
COPY gateway/ ./gateway/

# 设置环境变量
ENV PATH="/opt/venv/bin:$PATH" \
    PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8080/api/v1/health')" || exit 1

# 启动命令
CMD ["uvicorn", "gateway.app:app", "--host", "0.0.0.0", "--port", "8080"]
