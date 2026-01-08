'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useLocale } from '@/hooks/use-locale'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { authApi } from '@/lib/api/auth'
import { useTranslation } from '@/lib/i18n-client'
import type { ErrorHandlerConfig } from '@/lib/error-handler'

export function ForgotPasswordForm({
  className,
  ...props
}: React.ComponentProps<'div'>) {
  const router = useRouter()
  const locale = useLocale()
  const dict = useTranslation()

  const [formData, setFormData] = useState({
    email: '',
    code: '',
    newPassword: '',
    confirmPassword: '',
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isLoading, setIsLoading] = useState(false)
  const [isCodeSending, setIsCodeSending] = useState(false)
  const [codeSent, setCodeSent] = useState(false)
  const [countdown, setCountdown] = useState(0)

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.email) {
      newErrors.email = dict.auth.forgotPassword.errors.emailRequired
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = dict.auth.forgotPassword.errors.emailInvalid
    }

    if (!formData.code) {
      newErrors.code = dict.auth.forgotPassword.errors.codeRequired
    } else if (!/^\d{6}$/.test(formData.code)) {
      newErrors.code = dict.auth.forgotPassword.errors.codeInvalid
    }

    if (!formData.newPassword) {
      newErrors.newPassword = dict.auth.forgotPassword.errors.passwordRequired
    } else if (formData.newPassword.length < 8) {
      newErrors.newPassword = dict.auth.forgotPassword.errors.passwordTooShort
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = dict.auth.forgotPassword.errors.confirmPasswordRequired
    } else if (formData.newPassword !== formData.confirmPassword) {
      newErrors.confirmPassword = dict.auth.forgotPassword.errors.passwordMismatch
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSendCode = async () => {
    if (!formData.email) {
      setErrors({ email: dict.auth.forgotPassword.errors.emailRequired })
      return
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      setErrors({ email: dict.auth.forgotPassword.errors.emailInvalid })
      return
    }

    setIsCodeSending(true)
    setErrors({})

    const errorHandler: ErrorHandlerConfig = { showToast: true }

    try {
      await authApi.requestEmailCode({
        email: formData.email,
        scene: 'RESET_PASSWORD',
      }, errorHandler)

      setCodeSent(true)
      setCountdown(60)

      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer)
            return 0
          }
          return prev - 1
        })
      }, 1000)
    } catch {
      // 错误已在拦截器中通过 toast 显示
    } finally {
      setIsCodeSending(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    setIsLoading(true)
    setErrors({})

    const errorHandler: ErrorHandlerConfig = { showToast: true }

    try {
      await authApi.resetPassword({
        email: formData.email,
        code: formData.code,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword,
      }, errorHandler)

      router.push(`/${locale}/login`)
    } catch {
      // 错误已在拦截器中通过 toast 显示
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className={cn('flex flex-col gap-6', className)} {...props}>
      <Card>
        <CardHeader className="text-center">
          <CardTitle className="text-xl">{dict.auth.forgotPassword.title}</CardTitle>
          <CardDescription>
            {dict.auth.forgotPassword.description}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="email">{dict.auth.forgotPassword.email}</FieldLabel>
                <Input
                  id="email"
                  type="email"
                  placeholder={dict.auth.forgotPassword.emailPlaceholder}
                  value={formData.email}
                  onChange={(e) =>
                    setFormData({ ...formData, email: e.target.value })
                  }
                  disabled={isLoading}
                  aria-invalid={!!errors.email}
                />
                {errors.email && <FieldError>{errors.email}</FieldError>}
              </Field>

              <Field>
                <FieldLabel htmlFor="code">{dict.auth.forgotPassword.code}</FieldLabel>
                <div className="flex gap-2">
                  <Input
                    id="code"
                    type="text"
                    placeholder={dict.auth.forgotPassword.codePlaceholder}
                    value={formData.code}
                    onChange={(e) =>
                      setFormData({ ...formData, code: e.target.value })
                    }
                    disabled={isLoading}
                    aria-invalid={!!errors.code}
                    maxLength={6}
                  />
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleSendCode}
                    disabled={isCodeSending || countdown > 0 || isLoading}
                    className="whitespace-nowrap"
                  >
                    {countdown > 0
                      ? `${countdown}${dict.auth.forgotPassword.resendCountdown}`
                      : codeSent
                      ? dict.auth.forgotPassword.resendCode
                      : dict.auth.forgotPassword.sendCode}
                  </Button>
                </div>
                {errors.code && <FieldError>{errors.code}</FieldError>}
              </Field>

              <Field>
                <FieldLabel htmlFor="newPassword">{dict.auth.forgotPassword.newPassword}</FieldLabel>
                <Input
                  id="newPassword"
                  type="password"
                  placeholder={dict.auth.forgotPassword.passwordPlaceholder}
                  value={formData.newPassword}
                  onChange={(e) =>
                    setFormData({ ...formData, newPassword: e.target.value })
                  }
                  disabled={isLoading}
                  aria-invalid={!!errors.newPassword}
                />
                {errors.newPassword && <FieldError>{errors.newPassword}</FieldError>}
              </Field>

              <Field>
                <FieldLabel htmlFor="confirmPassword">{dict.auth.forgotPassword.confirmPassword}</FieldLabel>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder={dict.auth.forgotPassword.confirmPasswordPlaceholder}
                  value={formData.confirmPassword}
                  onChange={(e) =>
                    setFormData({ ...formData, confirmPassword: e.target.value })
                  }
                  disabled={isLoading}
                  aria-invalid={!!errors.confirmPassword}
                />
                {errors.confirmPassword && (
                  <FieldError>{errors.confirmPassword}</FieldError>
                )}
              </Field>

              <Field>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? dict.auth.forgotPassword.submitting : dict.auth.forgotPassword.submit}
                </Button>
                <FieldDescription className="text-center">
                  {dict.auth.forgotPassword.rememberPassword} <a href={`/${locale}/login`} className="underline">{dict.auth.forgotPassword.login}</a>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
