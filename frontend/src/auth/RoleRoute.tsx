import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import type { Rol } from '../api/types'
import { useAuth } from './AuthContext'

export function RoleRoute({
  allow,
  children,
}: {
  allow: Rol[]
  children: ReactNode
}) {
  const { user } = useAuth()

  if (!user) return <Navigate to="/login" replace />
  if (!allow.includes(user.rol)) return <Navigate to="/sin-acceso" replace />
  return <>{children}</>
}
