import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import {
  ArrowRight,
  Eye,
  EyeOff,
  LockKeyhole,
  ShieldCheck,
  UserRound,
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { InlineSpinner } from '../components/Loading'
import { BrandMark } from '../components/Brand'
import { homeForRole } from '../po01/permissions'

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [usuario, setUsuario] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (user) return <Navigate to={homeForRole(user.rol)} replace />

  const destination = (location.state as { from?: string } | null)?.from ?? ''

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    if (!usuario.trim() || !password) {
      setError('Ingrese su usuario y contraseña.')
      return
    }
    setLoading(true)
    try {
      const nextSession = await login(usuario.trim(), password)
      const fallback = homeForRole(nextSession.usuario.rol)
      navigate(destination && destination !== '/' ? destination : fallback, {
        replace: true,
      })
    } catch (loginError) {
      setError(
        loginError instanceof Error
          ? loginError.message
          : 'No se pudo iniciar sesión.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <section className="login-visual" aria-label="Moliselva">
        <div className="login-visual-overlay" aria-hidden="true" />
        <div className="login-visual-inner">
          <div className="login-visual-brand">
            <BrandMark light />
            <div>
              <strong>Moliselva</strong>
              <span>Planta Tarapoto</span>
            </div>
          </div>

          <div className="login-visual-copy">
            <span className="login-kicker">Recepción de maíz</span>
            <h1>Del campo a la balanza, sin perder el hilo.</h1>
            <p>
              Pesaje, humedad y merma en un solo registro. El kardex de maíz
              recibe la carga cuando el control de calidad la acepta.
            </p>
          </div>

          <div className="login-visual-foot">
            <div className="login-visual-facts">
              <span>Balanza</span>
              <span>Calidad</span>
              <span>Kardex</span>
            </div>
            <p className="photo-credit">
              Foto:{' '}
              <a
                href="https://commons.wikimedia.org/wiki/File:Corn_field_in_Mexico.jpg"
                target="_blank"
                rel="noreferrer"
              >
                Corn field in Mexico
              </a>{' '}
              · Tomascastelazo · CC BY-SA 4.0
            </p>
          </div>
        </div>
      </section>

      <section className="login-panel">
        <div className="login-panel-inner">
          <div className="login-card">
            <div className="login-card-head">
              <span className="overline">Acceso operativo</span>
              <h2>Iniciar sesión</h2>
              <p>Use la cuenta asignada a su rol en planta.</p>
            </div>

            <form className="login-form" onSubmit={handleSubmit}>
              <label className="field">
                <span>Usuario</span>
                <div className="input-with-icon">
                  <UserRound size={17} aria-hidden="true" />
                  <input
                    type="text"
                    value={usuario}
                    onChange={(event) => setUsuario(event.target.value)}
                    placeholder="Nombre de usuario"
                    autoComplete="username"
                    autoCapitalize="none"
                    spellCheck={false}
                    aria-invalid={Boolean(error)}
                    autoFocus
                  />
                </div>
              </label>

              <label className="field">
                <span>Contraseña</span>
                <div className="input-with-icon">
                  <LockKeyhole size={17} aria-hidden="true" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    placeholder="Contraseña"
                    autoComplete="current-password"
                    aria-invalid={Boolean(error)}
                  />
                  <button
                    type="button"
                    className="input-action"
                    onClick={() => setShowPassword((value) => !value)}
                    aria-label={
                      showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'
                    }
                  >
                    {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                  </button>
                </div>
              </label>

              {error && (
                <p className="form-error" role="alert">
                  {error}
                </p>
              )}

              <button
                type="submit"
                className="button button-primary button-block login-submit"
                disabled={loading}
              >
                {loading ? <InlineSpinner /> : null}
                {loading ? 'Verificando…' : 'Ingresar'}
                {!loading && <ArrowRight size={17} aria-hidden="true" />}
              </button>
            </form>

            <div className="login-assurance">
              <ShieldCheck size={17} aria-hidden="true" />
              <span>
                El acceso queda asociado a su usuario. Las decisiones pendientes
                solo puede autorizarlas el jefe de planta.
              </span>
            </div>
          </div>

          <p className="login-legal">
            © 2026 Moliselva · Planta Tarapoto · Uso interno
          </p>
        </div>
      </section>
    </div>
  )
}
