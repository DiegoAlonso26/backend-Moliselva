import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { PageHeader } from '../components/PageHeader'
import { po01Api } from '../po01/api'
import { navigation } from './navigation'
import { todayLima } from './ResourcePage'
import { formatKg } from '../lib/format'

export function HomePage() {
  const { user } = useAuth()
  const [summary, setSummary] = useState<{ count: number; pending: number; stock: number } | null>(null)
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    let active = true
    setError('')
    Promise.all([po01Api.getRecepciones(todayLima()), po01Api.getStockMaiz()])
      .then(([recepciones, stock]) => { if (active) setSummary({ count: recepciones.length, pending: recepciones.filter(r => r.estado === 'PENDIENTE').length, stock: stock.stockKg }) })
      .catch(e => { if (active) setError(e instanceof Error ? e.message : 'No se pudo cargar el resumen.') })
    return () => { active = false }
  }, [retry])
  return <div className="stack-lg"><PageHeader eyebrow="Sistema Moliselva" title="Inicio" description={`Bienvenido, ${user?.nombres}. Consulte los datos de la planta y acceda a sus operaciones.`} />
    {error ? <div className="panel" role="alert"><p>{error}</p><button className="button button-secondary" onClick={() => setRetry(n => n + 1)}>Reintentar</button></div>
      : summary ? <div className="home-summary"><article className="panel"><small>Recepciones de hoy · Lima</small><strong>{summary.count}</strong></article><article className="panel"><small>Pendientes de decisión hoy</small><strong>{summary.pending}</strong></article><article className="panel"><small>Stock actual de maíz</small><strong>{formatKg(summary.stock)}</strong></article></div> : <p role="status">Cargando resumen…</p>}
    <h2>Accesos disponibles</h2><div className="home-links">{navigation.filter(n => n.to !== '/inicio' && user && n.read.includes(user.rol)).map(n => <Link className="panel home-link" key={n.to} to={n.to}><small>{n.group}</small><strong>{n.label}</strong><span>Ir a la sección →</span></Link>)}</div>
  </div>
}
