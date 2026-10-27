import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
  BookOpen,
  CalendarDays,
  LogOut,
  Menu,
  Truck,
  X,
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { formatLongDate } from '../lib/format'
import { roleLabel } from '../po01/permissions'
import { Brand } from './Brand'

const navItems = [
  { to: '/recepcion', label: 'Recepción de maíz', shortLabel: 'Recepción', icon: Truck },
  { to: '/kardex', label: 'Kardex de maíz', shortLabel: 'Kardex', icon: BookOpen },
]

export function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [mobileOpen, setMobileOpen] = useState(false)

  useEffect(() => {
    setMobileOpen(false)
    window.scrollTo(0, 0)
  }, [location.pathname])

  const current = [...navItems]
    .sort((a, b) => b.to.length - a.to.length)
    .find((item) => location.pathname.startsWith(item.to))

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  if (!user) return null

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header-inner">
          <Link to="/" className="header-brand" aria-label="Moliselva, inicio">
            <Brand />
          </Link>

          <nav className="top-nav" aria-label="Navegación principal">
            {navItems.map((item) => {
              const Icon = item.icon
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `top-nav-link ${isActive ? 'top-nav-link-active' : ''}`
                  }
                >
                  <Icon size={17} aria-hidden="true" />
                  <span>{item.shortLabel}</span>
                </NavLink>
              )
            })}
          </nav>

          <div className="header-tools">
            <span className="header-date">
              <CalendarDays size={15} aria-hidden="true" />
              {formatLongDate()}
            </span>
            <span className="header-separator" aria-hidden="true" />
            <div className="header-user">
              <span className="header-avatar" aria-hidden="true">
                {user.nombres.charAt(0).toUpperCase()}
              </span>
              <span className="header-user-copy">
                <strong>{user.nombres}</strong>
                <small>{roleLabel(user.rol)}</small>
              </span>
              <button
                type="button"
                className="header-logout"
                onClick={handleLogout}
                aria-label="Cerrar sesión"
                title="Cerrar sesión"
              >
                <LogOut size={17} />
              </button>
            </div>
            <button
              type="button"
              className="header-menu-button"
              onClick={() => setMobileOpen((value) => !value)}
              aria-label={mobileOpen ? 'Cerrar menú' : 'Abrir menú'}
              aria-expanded={mobileOpen}
            >
              {mobileOpen ? <X size={21} /> : <Menu size={21} />}
            </button>
          </div>
        </div>

        {mobileOpen && (
          <nav className="mobile-nav" aria-label="Navegación móvil">
            {navItems.map((item) => {
              const Icon = item.icon
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `mobile-nav-link ${isActive ? 'mobile-nav-link-active' : ''}`
                  }
                >
                  <Icon size={18} aria-hidden="true" />
                  <span>{item.label}</span>
                </NavLink>
              )
            })}
            <button type="button" className="mobile-logout" onClick={handleLogout}>
              <LogOut size={18} aria-hidden="true" />
              Cerrar sesión
            </button>
          </nav>
        )}
      </header>

      <div className="workspace-bar">
        <div className="workspace-bar-inner">
          <span className="workspace-context">
            Operaciones <span aria-hidden="true">/</span>{' '}
            <strong>{current?.label ?? 'Recepción de maíz'}</strong>
          </span>
          <span className="plant-status">
            <span className="plant-status-dot" aria-hidden="true" />
            Planta operativa
          </span>
        </div>
      </div>

      <main className="page-content">
        <Outlet />
      </main>

      <footer className="app-footer">
        <span>Moliselva · Recepción y kardex de maíz</span>
        <span>Planta Tarapoto · Uso interno</span>
      </footer>
    </div>
  )
}
