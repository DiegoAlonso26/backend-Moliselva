import { Link } from 'react-router-dom'
import { ArrowLeft, ShieldX } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { PageHeader } from '../components/PageHeader'
import { roleLabel } from '../lib/permissions'

export function NoAccessPage() {
  const { user } = useAuth()

  return (
    <div className="stack-lg">
      <PageHeader
        eyebrow="Acceso restringido"
        title="Sin permiso para esta sección"
        description="Su rol no tiene acceso a la pantalla solicitada."
      />
      <section className="panel no-access-panel">
        <div className="no-access-icon">
          <ShieldX size={30} />
        </div>
        <h2>Acceso no autorizado</h2>
        <p>
          La sesión actual corresponde al rol{' '}
          <strong>{user ? roleLabel(user.rol) : 'sin rol'}</strong>. Si necesita
          realizar esta operación, solicite el cambio de permisos al administrador.
        </p>
        <Link to="/" className="button button-primary">
          <ArrowLeft size={17} />
          Volver al panel
        </Link>
      </section>
    </div>
  )
}
