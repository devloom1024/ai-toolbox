"use client"

import * as React from "react"
import { Globe } from "lucide-react"
import { usePathname, useRouter } from "next/navigation"

import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { i18n } from "@/lib/i18n-config"

const LANGUAGES = [
  { value: "en-US", label: "English" },
  { value: "zh-CN", label: "简体中文" },
] as const

type LanguageOption = (typeof LANGUAGES)[number]

type LanguageToggleProps = {
  label: string
  srLabel: string
}

export function LanguageToggle({ label, srLabel }: LanguageToggleProps) {
  const router = useRouter()
  const pathname = usePathname()

  const segments = React.useMemo(() => {
    return pathname?.split("/").filter(Boolean) ?? []
  }, [pathname])

  const currentLocale = (segments[0] as LanguageOption["value"]) ?? i18n.defaultLocale
  const restSegments = segments.slice(1)

  const handleChange = React.useCallback(
    (nextLocale: string) => {
      if (nextLocale === currentLocale) return
      const nextPath = `/${nextLocale}${restSegments.length ? `/${restSegments.join("/")}` : ""}`
      router.push(nextPath)
      router.refresh()
    },
    [currentLocale, restSegments, router]
  )

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          size="icon"
          variant="ghost"
          className="h-8 w-8"
          aria-label={srLabel}
        >
          <Globe className="h-4 w-4" />
          <span className="sr-only">{srLabel}</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-40">
        <DropdownMenuLabel>{label}</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuRadioGroup value={currentLocale} onValueChange={handleChange}>
          {LANGUAGES.map((option: LanguageOption) => (
            <DropdownMenuRadioItem key={option.value} value={option.value}>
              {option.label}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
