import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { po01Api } from '../po01/api'
import type { Po01Session } from '../po01/types'
import { ApiError, request, SESSION_KEY } from '../shared/http'

function readSession(): Po01Session | null {
  try {
    const value = JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null')
    return value?.usuario?.id && value?.token ? value : null
  } catch { return null }
}
interface AuthValue {
  session: Po01Session | null
  user: Po01Session['usuario'] | null
  checking: boolean
  sessionError: string
  refreshSession: () => Promise<void>
  login: (usuario: string, password: string) => Promise<Po01Session>
  logout: () => void
}
const AuthContext = createContext<AuthValue | null>(null)
export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState(readSession)
  const [checking, setChecking] = useState(() => Boolean(readSession()))
  const [sessionError, setSessionError] = useState('')
  const logout = useCallback(() => {
    sessionStorage.removeItem(SESSION_KEY)
    setSession(null)
    setSessionError('')
    setChecking(false)
  }, [])
  const refreshSession = useCallback(async () => {
    const current = readSession()
    if (!current) { logout(); return }
    setChecking(true)
    setSessionError('')
    try {
      const usuario = await request<Po01Session['usuario']>('/auth/me')
      if (readSession()?.token !== current.token) return
      const updated = { ...current, usuario }
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(updated))
      setSession(updated)
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) logout()
      else setSessionError(error instanceof Error ? error.message : 'No se pudo validar la sesión.')
    } finally { setChecking(false) }
  }, [logout])
  useEffect(() => {
    if (readSession()) void refreshSession()
    const refresh = () => { void refreshSession() }
    window.addEventListener('moliselva:unauthorized', logout)
    window.addEventListener('moliselva:forbidden', refresh)
    return () => {
      window.removeEventListener('moliselva:unauthorized', logout)
      window.removeEventListener('moliselva:forbidden', refresh)
    }
  }, [logout, refreshSession])
  const login = useCallback(async (usuario: string, password: string) => {
    const next = await po01Api.login(usuario, password)
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(next))
    setSession(next)
    setSessionError('')
    setChecking(false)
    return next
  }, [])
  const value = useMemo(() => ({ session, user: session?.usuario ?? null, checking,
    sessionError, refreshSession, login, logout }), [session, checking, sessionError, refreshSession, login, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth debe usarse dentro de AuthProvider.')
  return value
}
