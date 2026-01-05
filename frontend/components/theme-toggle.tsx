"use client"

import * as React from "react"
import { Laptop, Moon, Sun } from "lucide-react"
import { useTheme } from "next-themes"

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

const THEME_OPTIONS = [
  { value: "light", icon: Sun },
  { value: "dark", icon: Moon },
  { value: "system", icon: Laptop },
] as const

type ThemeOption = (typeof THEME_OPTIONS)[number]

type ThemeToggleProps = {
  label: string
  srLabel: string
  options: {
    light: string
    dark: string
    system: string
  }
}

export function ThemeToggle({ label, options, srLabel }: ThemeToggleProps) {
  const { theme, setTheme } = useTheme()
  const [mounted, setMounted] = React.useState(false)

  React.useEffect(() => setMounted(true), [])

  const selectedTheme = theme ?? "system"
  const icon =
    selectedTheme === "system" ? (
      <Laptop className="h-4 w-4" />
    ) : (
      <>
        <Sun className="h-4 w-4 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
        <Moon className="absolute h-4 w-4 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
      </>
    )

  const handleChange = React.useCallback(
    (nextTheme: string) => {
      setTheme(nextTheme)
    },
    [setTheme]
  )

  if (!mounted) {
    return (
      <Button size="icon" variant="ghost" className="h-8 w-8" aria-label={srLabel}>
        {icon}
        <span className="sr-only">{srLabel}</span>
      </Button>
    )
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          size="icon"
          variant="ghost"
          className="h-8 w-8"
          aria-label={srLabel}
        >
          {icon}
          <span className="sr-only">{srLabel}</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-44">
        <DropdownMenuLabel>{label}</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuRadioGroup value={selectedTheme} onValueChange={handleChange}>
          {THEME_OPTIONS.map((option: ThemeOption) => (
            <DropdownMenuRadioItem key={option.value} value={option.value}>
              <option.icon className="mr-2 h-4 w-4" />
              {options[option.value]}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
