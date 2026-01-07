import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // 启用静态导出模式 - 运行时不需要 Node.js 服务器
  output: 'export',

  // URL 末尾添加斜杠（适配静态服务器）
  trailingSlash: true,

  // 禁用图片优化（静态导出不支持）
  images: {
    unoptimized: true,
  },
};

export default nextConfig;
