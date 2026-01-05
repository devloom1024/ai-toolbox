import "server-only"

import { i18n, type Locale } from "@/lib/i18n-config"

const dictionaries = {
  "en-US": () => import("@/dictionaries/en-US.json").then((module) => module.default),
  "zh-CN": () => import("@/dictionaries/zh-CN.json").then((module) => module.default),
} as const

export type Dictionary = Awaited<ReturnType<(typeof dictionaries)[Locale]>>

export async function getDictionary(locale: Locale): Promise<Dictionary> {
  const loadDictionary =
    dictionaries[locale] ?? dictionaries[i18n.defaultLocale]

  return loadDictionary()
}
