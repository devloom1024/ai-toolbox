'use client'

import { LanguageToggle } from '@/components/language-toggle'
import { RegisterForm } from '@/components/register-form'
import { ThemeToggle } from '@/components/theme-toggle'
import { useTranslation } from '@/lib/i18n-client'

export default function RegisterPage() {
  const dictionary = useTranslation()

  return (
    <div className="bg-muted relative flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10">
      {/* 右上角按钮 */}
      <div className="absolute right-4 top-4 flex items-center gap-2">
        <LanguageToggle
          label={dictionary.header.actions.language.menu}
          srLabel={dictionary.header.actions.language.sr}
        />
        <ThemeToggle
          label={dictionary.header.actions.theme.menu}
          srLabel={dictionary.header.actions.theme.sr}
          options={dictionary.header.actions.theme.options}
        />
      </div>

      <div className="flex w-full max-w-sm flex-col gap-6">
        <div className="flex flex-col gap-2 text-center">
          <h1 className="text-2xl font-bold">AI Toolbox</h1>
          <p className="text-muted-foreground text-balance text-sm">
            {dictionary.auth.register.description}
          </p>
        </div>
        <RegisterForm />
      </div>
    </div>
  )
}
