import type { ReactNode } from "react";
import { getDictionary } from "@/lib/dictionaries";
import { I18nProvider } from "@/lib/i18n-client";
import { i18n, type Locale } from "@/lib/i18n-config";

/**
 * 生成静态参数
 * 用于为每个支持的语言生成静态页面
 */
export function generateStaticParams() {
  return i18n.locales.map((locale) => ({ locale }));
}

/**
 * 禁用动态参数
 * 确保只生成 generateStaticParams 返回的参数
 */
export const dynamicParams = false;

/**
 * 本地化布局组件
 * 在构建时加载翻译数据，并通过 I18nProvider 传递给客户端组件
 *
 * 这是一个服务端组件：
 * - 在构建时（而非运行时）执行
 * - 调用 getDictionary() 加载翻译 JSON
 * - 将翻译数据注入到客户端组件树中
 */
export default async function LocaleLayout({
  children,
  params,
}: {
  children: ReactNode;
  params: Promise<{ locale: string }>;
}) {
  // 获取当前语言并断言类型
  const { locale } = await params;
  const validLocale = locale as Locale;

  // 在构建时加载翻译字典
  // 这个数据会被打包进静态 HTML 和 JavaScript bundle
  const dictionary = await getDictionary(validLocale);

  return (
    // 将翻译数据传递给客户端组件
    <I18nProvider dictionary={dictionary}>{children}</I18nProvider>
  );
}
