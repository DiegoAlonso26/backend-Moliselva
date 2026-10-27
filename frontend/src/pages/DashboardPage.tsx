import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  AlertTriangle,
  ArrowUpRight,
  Boxes,
  Clock3,
  Factory,
  PackageCheck,
  RefreshCw,
  ShieldAlert,
  ShoppingCart,
  Truck,
  Warehouse,
} from 'lucide-react'
import { api } from '../api'
import type {
  DashboardData,
  Despacho,
  Recepcion,
} from '../api/types'
import { Badge } from '../components/Badge'
import { EmptyState } from '../components/EmptyState'
import { LoadingBlock, InlineSpinner } from '../components/Loading'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { useAuth } from '../auth/AuthContext'
import {
  formatDateTime,
  formatKg,
  formatNumber,
  formatSacos,
} from '../lib/format'
import { roleLabel } from '../lib/permissions'

const movementTone = {
  ENTRADA: 'success',
  SALIDA: 'danger',
  AJUSTE_ENTRADA: 'info',
  AJUSTE_SALIDA: 'warning',
} as const

const movementLabel = {
  ENTRADA: 'Entrada',
  SALIDA: 'Salida',
  AJUSTE_ENTRADA: 'Ajuste +',
  AJUSTE_SALIDA: 'Ajuste −',
} as const

export function DashboardPage() {
  const { user } = useAuth()
  const [data, setData] = useState<DashboardData | null>(null)
  const [recepciones, setRecepciones] = useState<Recepcion[]>([])
  const [despachos, setDespachos] = useState<Despacho[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async (refresh = false) => {
    if (refresh) setRefreshing(true)
    else setLoading(true)
    setError('')
    try {
      const [dashboard, receptionRows, dispatchRows] = await Promise.all([
        api.getDashboard(),
        api.getRecepciones(),
        api.getDespachos(),
      ])
      setData(dashboard)
      setRecepciones(receptionRows)
      setDespachos(dispatchRows)
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : 'No se pudo cargar el panel.',
      )
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  if (loading && !data) return <LoadingBlock label="Preparando el panel…" />

  if (error && !data) {
    return (
      <div className="panel error-panel">
        <AlertTriangle size={24} />
        <h2>No se pudo cargar el panel</h2>
        <p>{error}</p>
        <button type="button" className="button button-primary" onClick={() => void load()}>
          Reintentar
        </button>
      </div>
    )
  }

  if (!data || !user) return null

  const firstName = user.nombres.split(' ')[0]
  const pendingReceptions = recepciones.filter(
    (row) => row.estado === 'PENDIENTE',
  ).length
  const draftDispatches = despachos.filter((row) => row.estado === 'BORRADOR').length
  const alerts = data.alertas
  const quickTasks = [
    {
      label: 'Recepciones por autorizar',
      value: pendingReceptions,
      to: '/recepcion',
      icon: ShieldAlert,
    },
    {
      label: 'Despachos en borrador',
      value: draftDispatches,
      to: '/despacho',
      icon: PackageCheck,
    },
    {
      label: 'Productos con stock bajo',
      value: alerts.length,
      to: '/inventario',
      icon: AlertTriangle,
    },
  ]

  return (
    <div className="stack-lg">
      <PageHeader
        eyebrow="Panel de control"
        title={`Hola, ${firstName}`}
        description={`Resumen de la operación de Moliselva. Sesión activa como ${roleLabel(user.rol).toLowerCase()}.`}
        actions={
          <button
            type="button"
            className="button button-secondary"
            onClick={() => void load(true)}
            disabled={refreshing}
          >
            {refreshing ? <InlineSpinner /> : <RefreshCw size={17} />}
            Actualizar
          </button>
        }
      />

      {error && (
        <div className="inline-alert inline-alert-error">
          <AlertTriangle size={18} />
          {error}
        </div>
      )}

      <section className="stats-grid" aria-label="Indicadores del día">
        <StatCard
          icon={Warehouse}
          label="Stock de producto terminado"
          value={formatKg(data.stock.total_producto_kg)}
          helper={`${formatSacos(data.stock.total_sacos)} en 15 productos`}
          tone="green"
        />
        <StatCard
          icon={Truck}
          label="Recepciones registradas hoy"
          value={formatNumber(data.recepciones_hoy, 0)}
          helper={
            pendingReceptions
              ? `${pendingReceptions} pendiente${pendingReceptions === 1 ? '' : 's'} de autorización`
              : 'Sin recepciones pendientes'
          }
          tone="gold"
        />
        <StatCard
          icon={Factory}
          label="Lotes en proceso"
          value={formatNumber(data.lotes_en_proceso, 0)}
          helper="Producción registrada en planta"
          tone="blue"
        />
        <StatCard
          icon={ShoppingCart}
          label="Pedidos pendientes"
          value={formatNumber(data.pedidos_pendientes, 0)}
          helper="Pedidos por preparar o despachar"
          tone="red"
        />
      </section>

      <section className="dashboard-main-grid">
        <article className="panel panel-wide">
          <header className="panel-head">
            <div>
              <h2>Movimientos recientes</h2>
              <p>Últimas entradas y salidas registradas en el inventario.</p>
            </div>
            <Link to="/inventario" className="text-link">
              Ver kardex <ArrowUpRight size={15} />
            </Link>
          </header>

          {data.movimientos.length === 0 ? (
            <EmptyState
              icon={Boxes}
              title="Sin movimientos"
              description="Los movimientos aparecerán cuando registre una recepción, un lote o un despacho."
            />
          ) : (
            <div className="table-wrap">
              <table className="data-table recent-table">
                <thead>
                  <tr>
                    <th>Movimiento</th>
                    <th>Ítem</th>
                    <th className="align-right">Cantidad</th>
                    <th>Referencia</th>
                    <th>Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {data.movimientos.map((movement) => {
                    const signed =
                      movement.tipo_movimiento === 'ENTRADA' ||
                      movement.tipo_movimiento === 'AJUSTE_ENTRADA'
                    return (
                      <tr key={movement.id_movimiento}>
                        <td>
                          <Badge tone={movementTone[movement.tipo_movimiento]} dot>
                            {movementLabel[movement.tipo_movimiento]}
                          </Badge>
                        </td>
                        <td>
                          <div className="cell-main">
                            <strong>{movement.item}</strong>
                            <span>{movement.tipo_item === 'PRODUCTO' ? 'Producto' : 'Insumo'}</span>
                          </div>
                        </td>
                        <td className={`align-right numeric ${signed ? 'text-success' : 'text-danger'}`}>
                          {signed ? '+' : '−'}
                          {formatNumber(movement.cantidad)} kg
                        </td>
                        <td className="muted">{movement.referencia ?? 'Sin referencia'}</td>
                        <td className="muted nowrap">{formatDateTime(movement.fecha_hora)}</td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </article>

        <aside className="dashboard-side">
          <article className="panel">
            <header className="panel-head">
              <div>
                <h2>Tareas de hoy</h2>
                <p>Pendientes que requieren atención.</p>
              </div>
            </header>
            <div className="task-list">
              {quickTasks.map((task) => {
                const Icon = task.icon
                return (
                  <Link key={task.label} to={task.to} className="task-item">
                    <div className={`task-icon ${task.value > 0 ? 'task-icon-active' : ''}`}>
                      <Icon size={19} />
                    </div>
                    <div>
                      <strong>{task.label}</strong>
                      <span>{task.value > 0 ? `${task.value} pendiente${task.value === 1 ? '' : 's'}` : 'Al día'}</span>
                    </div>
                    <ArrowUpRight size={16} className="task-arrow" />
                  </Link>
                )
              })}
            </div>
          </article>

          <article className="panel">
            <header className="panel-head">
              <div>
                <h2>Alertas de stock</h2>
                <p>Productos con 80 kg o menos.</p>
              </div>
            </header>
            {alerts.length === 0 ? (
              <div className="all-clear">
                <PackageCheck size={22} />
                <div>
                  <strong>Sin alertas</strong>
                  <span>Los productos tienen stock suficiente para operar.</span>
                </div>
              </div>
            ) : (
              <div className="alert-list">
                {alerts.map((product) => (
                  <div key={product.id_producto} className="alert-row">
                    <div>
                      <strong>{product.nombre}</strong>
                      <span>{product.especie} · {product.fase}</span>
                    </div>
                    <Badge tone="danger">{formatKg(product.stock_kg)}</Badge>
                  </div>
                ))}
              </div>
            )}
          </article>
        </aside>
      </section>

      <section className="panel">
        <header className="panel-head">
          <div>
            <h2>Ruta de operación</h2>
            <p>Los cuatro procesos priorizados del sistema.</p>
          </div>
        </header>
        <div className="process-grid">
          <Link to="/recepcion" className="process-card">
            <span className="process-number">01</span>
            <Truck size={22} />
            <strong>Recepción de maíz</strong>
            <span>Peso, humedad y autorización del lote.</span>
          </Link>
          <Link to="/produccion" className="process-card">
            <span className="process-number">02</span>
            <Factory size={22} />
            <strong>Producción por lote</strong>
            <span>Fórmula, consumo real y producto terminado.</span>
          </Link>
          <Link to="/inventario" className="process-card">
            <span className="process-number">03</span>
            <Boxes size={22} />
            <strong>Inventario</strong>
            <span>Stock en cuadrícula y kardex trazable.</span>
          </Link>
          <Link to="/despacho" className="process-card">
            <span className="process-number">04</span>
            <ShoppingCart size={22} />
            <strong>Venta y despacho</strong>
            <span>Pedido, validación de stock y salida.</span>
          </Link>
        </div>
        <div className="process-note">
          <Clock3 size={16} />
          <span>
            La sesión actual es de demostración: los cambios se conservan en este
            navegador y no afectan datos reales.
          </span>
        </div>
      </section>
    </div>
  )
}
