import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'
import { LoadingBlock } from '../components/Loading'

export function ProtectedRoute() {
  const { session, checking, sessionError, refreshSession, logout } = useAuth()
  const location = useLocation()

  if (checking) return <LoadingBlock label="Validando sesión…" />
  if (sessionError) return <div className="panel session-error" role="alert"><h1>No se pudo validar la sesión</h1><p>{sessionError}</p><button className="button button-primary" onClick={() => void refreshSession()}>Reintentar</button><button className="button button-secondary" onClick={logout}>Volver al acceso</button></div>

  if (!session) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: `${location.pathname}${location.search}` }}
      />
    )
  }

  return <Outlet />
}
