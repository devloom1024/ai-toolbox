/**
 * 客户端国际化 Context
 * 用于在客户端组件中访问翻译数据
 */

"use client";

import * as React from "react";
import type { Dictionary } from "./dictionaries";

/**
 * 国际化 Context
 * 存储翻译字典数据
 */
const I18nContext = React.createContext<Dictionary | null>(null);

/**
 * 国际化 Provider 组件
 * 在构建时从服务端组件接收翻译数据，并传递给客户端组件
 *
 * @param dictionary - 翻译字典数据（从服务端组件传递）
 * @param children - 子组件
 */
export function I18nProvider({
  dictionary,
  children,
}: {
  dictionary: Dictionary;
  children: React.ReactNode;
}) {
  return (
    <I18nContext.Provider value={dictionary}>{children}</I18nContext.Provider>
  );
}

/**
 * 使用翻译的 Hook
 * 在客户端组件中调用以获取翻译数据
 *
 * @returns Dictionary - 翻译字典对象
 * @throws Error - 如果在 I18nProvider 外部使用
 *
 * @example
 * ```tsx
 * 'use client'
 *
 * function MyComponent() {
 *   const t = useTranslation()
 *   return <h1>{t.dashboard.hero.title}</h1>
 * }
 * ```
 */
export function useTranslation(): Dictionary {
  const dictionary = React.useContext(I18nContext);

  if (!dictionary) {
    throw new Error(
      "useTranslation 必须在 I18nProvider 内部使用。" +
        "请确保在 [locale]/layout.tsx 中已正确设置 I18nProvider。"
    );
  }

  return dictionary;
}
