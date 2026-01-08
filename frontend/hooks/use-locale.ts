"use client"

import { usePathname } from "next/navigation"
import { i18n, type Locale } from "@/lib/i18n-config"

export function useLocale(): Locale {
  const pathname = usePathname()
  const segments = pathname?.split("/").filter(Boolean) ?? []
  const locale = segments[0] as Locale

  if (i18n.locales.includes(locale)) {
    return locale
  }

  return i18n.defaultLocale
}
