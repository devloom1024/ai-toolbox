'use client'

import { useEffect, useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { authApi } from '@/lib/api/auth'
import { useAuth } from '@/lib/auth-context'
import { Button } from '@/components/ui/button'

const ERROR_MESSAGES = {
  missingCodeOrState: 'Missing code or state parameter',
  invalidState: 'Invalid state parameter',
  loginFailed: 'Login failed, please try again',
  networkError: 'Network error, please try again'
}

export default function LinuxDoCallbackPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { login } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const code = searchParams.get('code')
    const state = searchParams.get('state')

    if (!code || !state) {
      setError(ERROR_MESSAGES.missingCodeOrState)
      setLoading(false)
      return
    }

    const savedState = sessionStorage.getItem('linuxdo_oauth_state')
    if (savedState !== state) {
      setError(ERROR_MESSAGES.invalidState)
      setLoading(false)
      return
    }

    const exchangeToken = async () => {
      try {
        const response = await authApi.linuxDoCallback(code, state)

        if (response.code === 0 && response.data) {
          await login(response.data.accessToken, response.data.refreshToken)

          const redirectLocale = sessionStorage.getItem('linuxdo_redirect_locale') || 'zh-CN'
          sessionStorage.removeItem('linuxdo_oauth_state')
          sessionStorage.removeItem('linuxdo_redirect_locale')

          router.push(`/${redirectLocale}/`)
        } else {
          setError(response.message || ERROR_MESSAGES.loginFailed)
        }
      } catch {
        setError(ERROR_MESSAGES.networkError)
      } finally {
        setLoading(false)
      }
    }

    exchangeToken()
  }, [searchParams, login, router])

  if (error) {
    const redirectLocale = sessionStorage.getItem('linuxdo_redirect_locale') || 'zh-CN'
    return (
      <div className="flex min-h-svh flex-col items-center justify-center gap-4">
        <h1 className="text-2xl font-bold">Login Failed</h1>
        <p className="text-muted-foreground">{error}</p>
        <Button onClick={() => router.push(`/${redirectLocale}/login`)}>
          Back to Login
        </Button>
      </div>
    )
  }

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4">
      <h1 className="text-2xl font-bold">Logging in...</h1>
      <p className="text-muted-foreground">Please wait while we complete the authentication.</p>
    </div>
  )
}
