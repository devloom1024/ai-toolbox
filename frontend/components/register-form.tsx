'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
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
import { useAuth } from '@/lib/auth-context'
import { useTranslation } from '@/lib/i18n-client'
import type { ErrorHandlerConfig } from '@/lib/error-handler'

export function RegisterForm({
  className,
  ...props
}: React.ComponentProps<'div'>) {
  const router = useRouter()
  const { login } = useAuth()
  const dict = useTranslation()

  const [formData, setFormData] = useState({
    email: '',
    nickname: '',
    code: '',
    password: '',
    confirmPassword: '',
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isLoading, setIsLoading] = useState(false)
  const [isCodeSending, setIsCodeSending] = useState(false)
  const [codeSent, setCodeSent] = useState(false)
  const [countdown, setCountdown] = useState(0)

  // 表单验证
  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.email) {
      newErrors.email = dict.auth.register.errors.emailRequired
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = dict.auth.register.errors.emailInvalid
    }

    if (!formData.nickname) {
      newErrors.nickname = dict.auth.register.errors.nicknameRequired
    } else if (formData.nickname.length < 2 || formData.nickname.length > 20) {
      newErrors.nickname = dict.auth.register.errors.nicknameTooShort
    }

    if (!formData.code) {
      newErrors.code = dict.auth.register.errors.codeRequired
    } else if (!/^\d{6}$/.test(formData.code)) {
      newErrors.code = dict.auth.register.errors.codeInvalid
    }

    if (!formData.password) {
      newErrors.password = dict.auth.register.errors.passwordRequired
    } else if (formData.password.length < 8) {
      newErrors.password = dict.auth.register.errors.passwordTooShort
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = dict.auth.register.errors.confirmPasswordRequired
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = dict.auth.register.errors.passwordMismatch
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  // 发送验证码
  const handleSendCode = async () => {
    if (!formData.email) {
      setErrors({ email: dict.auth.register.errors.emailRequired })
      return
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      setErrors({ email: dict.auth.register.errors.emailInvalid })
      return
    }

    setIsCodeSending(true)
    setErrors({})

    try {
      const response = await authApi.requestEmailCode({
        email: formData.email,
        scene: 'REGISTER',
      })

      if (response.code === 0) {
        setCodeSent(true)
        setCountdown(60)

        // 倒计时
        const timer = setInterval(() => {
          setCountdown((prev) => {
            if (prev <= 1) {
              clearInterval(timer)
              return 0
            }
            return prev - 1
          })
        }, 1000)
      } else {
        setErrors({ code: response.message || 'Failed to send code' })
      }
    } catch (error) {
      setErrors({ code: error instanceof Error ? error.message : 'Failed to send code' })
    } finally {
      setIsCodeSending(false)
    }
  }

  // 提交注册
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    setIsLoading(true)
    setErrors({})

    const errorHandler: ErrorHandlerConfig = {
      showToast: true,
      toastType: 'error',
    }

    try {
      const response = await authApi.register({
        email: formData.email,
        password: formData.password,
        code: formData.code,
        nickname: formData.nickname,
      }, errorHandler)

      if (response.code === 0 && response.data) {
        await login(response.data.token.accessToken, response.data.token.refreshToken)
        router.push('/')
      }
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
          <CardTitle className="text-xl">{dict.auth.register.title}</CardTitle>
          <CardDescription>
            {dict.auth.register.description}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="email">{dict.auth.register.email}</FieldLabel>
                <Input
                  id="email"
                  type="email"
                  placeholder={dict.auth.register.emailPlaceholder}
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
                <FieldLabel htmlFor="nickname">{dict.auth.register.nickname}</FieldLabel>
                <Input
                  id="nickname"
                  type="text"
                  placeholder={dict.auth.register.nicknamePlaceholder}
                  value={formData.nickname}
                  onChange={(e) =>
                    setFormData({ ...formData, nickname: e.target.value })
                  }
                  disabled={isLoading}
                  aria-invalid={!!errors.nickname}
                />
                {errors.nickname && <FieldError>{errors.nickname}</FieldError>}
              </Field>

              <Field>
                <FieldLabel htmlFor="code">{dict.auth.register.code}</FieldLabel>
                <div className="flex gap-2">
                  <Input
                    id="code"
                    type="text"
                    placeholder={dict.auth.register.codePlaceholder}
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
                      ? `${countdown}${dict.auth.register.resendCountdown}`
                      : codeSent
                      ? dict.auth.register.resendCode
                      : dict.auth.register.sendCode}
                  </Button>
                </div>
                {errors.code && <FieldError>{errors.code}</FieldError>}
              </Field>

              <Field>
                <FieldLabel htmlFor="password">{dict.auth.register.password}</FieldLabel>
                <Input
                  id="password"
                  type="password"
                  placeholder={dict.auth.register.passwordPlaceholder}
                  value={formData.password}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                  disabled={isLoading}
                  aria-invalid={!!errors.password}
                />
                {errors.password && <FieldError>{errors.password}</FieldError>}
              </Field>

              <Field>
                <FieldLabel htmlFor="confirmPassword">{dict.auth.register.confirmPassword}</FieldLabel>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder={dict.auth.register.confirmPasswordPlaceholder}
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

              {errors.submit && (
                <Field>
                  <FieldError>{errors.submit}</FieldError>
                </Field>
              )}

              <Field>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? dict.auth.register.submitting : dict.auth.register.submit}
                </Button>
                <FieldDescription className="text-center">
                  {dict.auth.register.hasAccount} <a href="../login" className="underline">{dict.auth.register.login}</a>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
