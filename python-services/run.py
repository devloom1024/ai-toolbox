#!/usr/bin/env python
"""
开发服务器启动脚本
"""
import os
import sys
from pathlib import Path

def main():
    """启动开发服务器"""
    # 设置工作目录
    project_root = Path(__file__).parent
    os.chdir(project_root)

    # 检查虚拟环境
    venv_path = project_root / ".venv"
    if not venv_path.exists():
        print("❌ 虚拟环境不存在，请先运行:")
        print("   uv venv")
        print("   source .venv/bin/activate  # Windows: .venv\\Scripts\\activate")
        print("   uv pip install -e '.[akshare,all]'")
        sys.exit(1)

    # 检查环境变量文件
    env_file = project_root / ".env"
    if not env_file.exists():
        print("⚠️  .env 文件不存在，使用默认配置")
        print("   建议: cp .env.example .env")

    # 启动 uvicorn
    print("🚀 启动 Python Services Gateway...")
    print("📖 文档地址: http://localhost:8080/docs")
    print("🔍 健康检查: http://localhost:8080/api/v1/health")
    print("\n按 Ctrl+C 停止服务\n")

    try:
        import uvicorn
        uvicorn.run(
            "gateway.app:app",
            host="0.0.0.0",
            port=8080,
            reload=True,
            log_level="info",
        )
    except ImportError:
        print("❌ uvicorn 未安装，请运行: uv pip install -e '.[akshare,all]'")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n\n✅ 服务已停止")

if __name__ == "__main__":
    main()
