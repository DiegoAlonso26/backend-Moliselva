import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { LogOut, Menu, X } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { roleLabel } from '../po01/permissions'
import { navigation } from '../workspace/navigation'
import { Brand } from './Brand'

export function AppLayout() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const [open, setOpen] = useState(false)
  useEffect(() => { setOpen(false); window.scrollTo(0, 0) }, [location.pathname])
  if (!user) return null
  const items = navigation.filter(item => item.read.includes(user.rol))
  const groups = ['Inicio', 'Operaciones', 'Inventario', 'Catálogos', 'Administración']
  return <div className="workspace-shell">
    <header className="workspace-header"><button className="icon-button workspace-menu" aria-label={open ? 'Cerrar menú' : 'Abrir menú'} aria-expanded={open} onClick={() => setOpen(!open)}>{open ? <X /> : <Menu />}</button><Link to="/inicio"><Brand /></Link><div className="workspace-account"><strong>{user.nombres}</strong><small>{roleLabel(user.rol)}</small></div><button className="icon-button" aria-label="Cerrar sesión" title="Cerrar sesión" onClick={logout}><LogOut size={20} /></button></header>
    {open && <button className="workspace-scrim" aria-label="Cerrar navegación" onClick={() => setOpen(false)} />}
    <aside className={`workspace-sidebar ${open ? 'workspace-sidebar-open' : ''}`}><nav aria-label="Navegación principal">{groups.map(group => {
      const links = items.filter(item => item.group === group)
      return links.length ? <div className="workspace-nav-group" key={group}><h2>{group}</h2>{links.map(item => <NavLink key={item.to} to={item.to} className={({ isActive }) => `workspace-nav-link ${isActive ? 'workspace-nav-active' : ''}`}>{item.label}</NavLink>)}</div> : null
    })}</nav><small className="workspace-sidebar-note">Moliselva · Planta Tarapoto</small></aside>
    <main className="workspace-content" key={user.rol}><Outlet /></main>
  </div>
}
