#!/bin/bash
# Linux/macOS 启动脚本

set -e

echo "Checking virtual environment..."
if [ ! -d ".venv" ]; then
    echo "❌ Virtual environment not found!"
    echo "Please run:"
    echo "  uv venv"
    echo "  source .venv/bin/activate"
    echo "  uv pip install -e '.[akshare,all]'"
    exit 1
fi

echo "Checking .env file..."
if [ ! -f ".env" ]; then
    echo "⚠️  Warning: .env file not found"
    echo "Consider running: cp .env.example .env"
fi

echo "🚀 Starting Python Services Gateway..."
echo "📖 Documentation: http://localhost:8081/docs"
echo "🔍 Health Check: http://localhost:8081/api/v1/health"
echo ""
echo "Press Ctrl+C to stop"
echo ""

source .venv/bin/activate
python run.py
