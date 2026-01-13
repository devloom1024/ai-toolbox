@echo off
REM Windows 启动脚本

echo Checking virtual environment...
if not exist ".venv" (
    echo Virtual environment not found!
    echo Please run:
    echo   uv venv
    echo   .venv\Scripts\activate
    echo   uv pip install -e ".[akshare,all]"
    exit /b 1
)

echo Checking .env file...
if not exist ".env" (
    echo Warning: .env file not found
    echo Consider running: copy .env.example .env
)

echo Starting Python Services Gateway...
echo Documentation: http://localhost:8081/docs
echo Health Check: http://localhost:8081/api/v1/health
echo.
echo Press Ctrl+C to stop
echo.

call .venv\Scripts\activate
python run.py
