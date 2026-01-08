import { toast } from 'sonner'

export interface ErrorHandlerConfig {
  /**
   * 是否显示 toast 提示
   * @default true
   */
  showToast?: boolean
  /**
   * 自定义 toast 消息，如果为空则使用后端返回的 message
   */
  toastMessage?: string
  /**
   * toast 类型
   * @default 'error'
   */
  toastType?: 'success' | 'error' | 'info' | 'warning'
}

const defaultConfig: Required<ErrorHandlerConfig> = {
  showToast: true,
  toastMessage: '',
  toastType: 'error',
}

function getConfig(config?: ErrorHandlerConfig): Required<ErrorHandlerConfig> {
  return { ...defaultConfig, ...config }
}

export function showApiError(message: string, config?: ErrorHandlerConfig): void {
  const cfg = getConfig(config)

  if (cfg.showToast) {
    toast[cfg.toastType](cfg.toastMessage || message)
  }
}

export function showApiSuccess(message: string, config?: ErrorHandlerConfig): void {
  const cfg = getConfig(config)

  if (cfg.showToast) {
    toast.success(cfg.toastMessage || message)
  }
}

export function getApiErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message
  }
  return '请求失败'
}
