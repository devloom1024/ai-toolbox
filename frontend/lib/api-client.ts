/**
 * API 客户端配置
 * 用于与 SpringBoot 后端进行交互
 */

/**
 * API 基础 URL
 * 从环境变量读取，默认为本地开发服务器地址
 */
export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

/**
 * SWR fetcher 函数
 * 用于处理 HTTP 请求和错误
 *
 * @param url - API 端点 URL
 * @returns Promise<T> - 解析后的 JSON 数据
 * @throws Error - 当请求失败时抛出错误
 */
export async function fetcher<T = unknown>(url: string): Promise<T> {
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    const error = new Error("API 请求失败");
    // 附加错误信息
    const errorInfo = {
      status: response.status,
      statusText: response.statusText,
      url,
    };
    throw Object.assign(error, errorInfo);
  }

  return response.json();
}

/**
 * 构建完整的 API URL
 *
 * @param endpoint - API 端点路径（例如：'/api/users'）
 * @returns 完整的 API URL
 */
export function getApiUrl(endpoint: string): string {
  // 确保 endpoint 以 / 开头
  const normalizedEndpoint = endpoint.startsWith("/")
    ? endpoint
    : `/${endpoint}`;

  return `${API_BASE_URL}${normalizedEndpoint}`;
}

/**
 * POST 请求辅助函数
 *
 * @param endpoint - API 端点路径
 * @param data - 要发送的数据
 * @returns Promise<T> - 解析后的响应数据
 */
export async function post<T = unknown, D = unknown>(
  endpoint: string,
  data: D
): Promise<T> {
  const url = getApiUrl(endpoint);
  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = new Error("API POST 请求失败");
    const errorInfo = {
      status: response.status,
      statusText: response.statusText,
      url,
    };
    throw Object.assign(error, errorInfo);
  }

  return response.json();
}

/**
 * PUT 请求辅助函数
 *
 * @param endpoint - API 端点路径
 * @param data - 要发送的数据
 * @returns Promise<T> - 解析后的响应数据
 */
export async function put<T = unknown, D = unknown>(
  endpoint: string,
  data: D
): Promise<T> {
  const url = getApiUrl(endpoint);
  const response = await fetch(url, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = new Error("API PUT 请求失败");
    const errorInfo = {
      status: response.status,
      statusText: response.statusText,
      url,
    };
    throw Object.assign(error, errorInfo);
  }

  return response.json();
}

/**
 * DELETE 请求辅助函数
 *
 * @param endpoint - API 端点路径
 * @returns Promise<T> - 解析后的响应数据
 */
export async function del<T = unknown>(endpoint: string): Promise<T> {
  const url = getApiUrl(endpoint);
  const response = await fetch(url, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    const error = new Error("API DELETE 请求失败");
    const errorInfo = {
      status: response.status,
      statusText: response.statusText,
      url,
    };
    throw Object.assign(error, errorInfo);
  }

  return response.json();
}
