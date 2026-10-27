import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { po01Api } from '../po01/api'
import type { Po01Session } from '../po01/types'

const SESSION_KEY = 'moliselva_session'

interface AuthContextValue {
  session: Po01Session | null
  user: Po01Session['usuario'] | null
  login: (usuario: string, password: string) => Promise<Po01Session>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function readSession(): Po01Session | null {
  const raw = window.sessionStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    const session = JSON.parse(raw) as Po01Session
    if (!session?.usuario?.id || !session?.token) return null
    return session
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Po01Session | null>(() => readSession())

  const login = useCallback(async (usuario: string, password: string) => {
    const nextSession = await po01Api.login(usuario, password)
    window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(nextSession))
    setSession(nextSession)
    return nextSession
  }, [])

  const logout = useCallback(() => {
    window.sessionStorage.removeItem(SESSION_KEY)
    setSession(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      user: session?.usuario ?? null,
      login,
      logout,
    }),
    [session, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider.')
  return context
}
