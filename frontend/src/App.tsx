import { HashRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { RoleRoute } from './auth/RoleRoute'
import { AppLayout } from './components/AppLayout'
import { ToastProvider } from './components/Toast'
import { KardexPage } from './po01/KardexPage'
import { LoginPage } from './pages/LoginPage'
import { NoAccessPage } from './pages/NoAccessPage'
import { HomePage } from './workspace/HomePage'
import { ResourcePage } from './workspace/ResourcePage'
import { resources } from './workspace/navigation'

export default function App() {
  return <HashRouter><AuthProvider><ToastProvider><Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route element={<ProtectedRoute />}><Route element={<AppLayout />}>
      <Route index element={<Navigate to="/inicio" replace />} />
      <Route path="/inicio" element={<HomePage />} />
      {resources.map(({ to, config }) => <Route key={to} path={to} element={<RoleRoute allow={config.read}><ResourcePage config={config} /></RoleRoute>} />)}
      <Route path="/kardex" element={<KardexPage />} />
      <Route path="/sin-acceso" element={<NoAccessPage />} />
    </Route></Route>
    <Route path="*" element={<Navigate to="/inicio" replace />} />
  </Routes></ToastProvider></AuthProvider></HashRouter>
}
