import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { homeForRole } from './permissions'

export function HomeRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={homeForRole(user.rol)} replace />
}
