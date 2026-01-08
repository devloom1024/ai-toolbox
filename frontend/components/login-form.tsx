'use client'

import { Suspense, useState } from 'react'
import { useRouter, useSearchParams, usePathname } from 'next/navigation'
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
import { useAuth } from '@/lib/auth-context'
import { useTranslation } from '@/lib/i18n-client'
import type { ErrorHandlerConfig } from '@/lib/error-handler'

function LoginFormContent({
  className,
  ...props
}: React.ComponentProps<'div'>) {
  const router = useRouter()
  const searchParams = useSearchParams()
  const locale = useLocale()
  const { login } = useAuth()
  const dict = useTranslation()

  // 获取重定向路径
  const redirectPath = searchParams.get('redirect') || '/'

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isLoading, setIsLoading] = useState(false)

  // 表单验证
  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.email) {
      newErrors.email = dict.auth.login.errors.emailRequired
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = dict.auth.login.errors.emailInvalid
    }

    if (!formData.password) {
      newErrors.password = dict.auth.login.errors.passwordRequired
    } else if (formData.password.length < 8) {
      newErrors.password = dict.auth.login.errors.passwordTooShort
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  // 提交登录
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
      const response = await authApi.login({
        identifier: formData.email,
        password: formData.password,
        type: 'EMAIL',
      }, errorHandler)

      if (response.code === 0 && response.data) {
        await login(response.data.accessToken, response.data.refreshToken)
        router.push(redirectPath)
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
          <CardTitle className="text-xl">{dict.auth.login.title}</CardTitle>
          <CardDescription>
            {dict.auth.login.description}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="email">{dict.auth.login.email}</FieldLabel>
                <Input
                  id="email"
                  type="email"
                  placeholder={dict.auth.login.emailPlaceholder}
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
                <div className="flex items-center">
                  <FieldLabel htmlFor="password">{dict.auth.login.password}</FieldLabel>
                   <a
                     href={`/${locale}/forgot-password`}
                     className="ml-auto text-sm underline-offset-4 hover:underline"
                   >
                    {dict.auth.login.forgotPassword}
                  </a>
                </div>
                <Input
                  id="password"
                  type="password"
                  placeholder={dict.auth.login.passwordPlaceholder}
                  value={formData.password}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                  disabled={isLoading}
                  aria-invalid={!!errors.password}
                />
                {errors.password && <FieldError>{errors.password}</FieldError>}
              </Field>

              {errors.submit && (
                <Field>
                  <FieldError>{errors.submit}</FieldError>
                </Field>
              )}

              <Field>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? dict.auth.login.submitting : dict.auth.login.submit}
                </Button>
                 <FieldDescription className="text-center">
                   {dict.auth.login.noAccount} <a href={`/${locale}/register`} className="underline">{dict.auth.login.register}</a>
                 </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

export function LoginForm(props: React.ComponentProps<'div'>) {
  return (
    <Suspense fallback={
      <div className="flex min-h-[400px] items-center justify-center">
        <div className="text-muted-foreground text-sm">Loading...</div>
      </div>
    }>
      <LoginFormContent {...props} />
    </Suspense>
  )
}
