import { useCallback, useEffect, useState } from 'react'
import {
  AlertTriangle,
  ArrowDownLeft,
  BookOpen,
  Download,
  RefreshCw,
  Scale,
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { Badge } from '../components/Badge'
import { EmptyState } from '../components/EmptyState'
import { InlineSpinner, LoadingBlock } from '../components/Loading'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { useToast } from '../components/Toast'
import {
  displayUser,
  downloadCsv,
  formatDateTime,
  formatKg,
} from '../lib/format'
import { po01Api } from './api'
import { canExportKardex } from './permissions'
import type { Movimiento } from './types'

const movementTone = {
  ENTRADA: 'success',
  AJUSTE_ENTRADA: 'info',
  AJUSTE_SALIDA: 'warning',
} as const

const movementLabel = {
  ENTRADA: 'Entrada',
  AJUSTE_ENTRADA: 'Ajuste +',
  AJUSTE_SALIDA: 'Ajuste −',
} as const

function fechaHoyLocal(): string {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function origen(row: Movimiento): string {
  return row.idRecepcion
    ? `Recepción #${row.idRecepcion}`
    : row.tipoMovimiento === 'ENTRADA'
      ? 'Saldo inicial'
      : 'Ajuste manual'
}

export function KardexPage() {
  const { user } = useAuth()
  const { notifyError } = useToast()
  const [movimientos, setMovimientos] = useState<Movimiento[]>([])
  const [stockKg, setStockKg] = useState(0)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async (refresh = false) => {
    if (refresh) setRefreshing(true)
    else setLoading(true)
    setError('')
    try {
      const [kardexRows, stock] = await Promise.all([
        po01Api.getKardex(),
        po01Api.getStockMaiz(),
      ])
      setMovimientos(kardexRows)
      setStockKg(stock.stockKg)
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : 'No se pudo cargar el kardex de maíz.',
      )
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const handleExport = () => {
    const rows: Array<Array<string | number>> = [
      ['Fecha y hora', 'Tipo', 'Cantidad kg', 'Saldo kg', 'Origen', 'Usuario'],
      ...movimientos.map((row) => [
        formatDateTime(row.fechaHora),
        movementLabel[row.tipoMovimiento],
        row.cantidadKg,
        row.saldoKg,
        origen(row),
        row.usuario,
      ]),
    ]
    downloadCsv(`kardex_maiz_${fechaHoyLocal()}.csv`, rows)
  }

  if (loading) return <LoadingBlock label="Cargando kardex de maíz…" />

  if (error) {
    return (
      <div className="panel error-panel">
        <AlertTriangle size={24} />
        <h2>No se pudo cargar el kardex</h2>
        <p>{error}</p>
        <button type="button" className="button button-primary" onClick={() => void load()}>
          Reintentar
        </button>
      </div>
    )
  }

  const lastMovement = movimientos.at(-1)

  return (
    <div className="stack-lg">
      <PageHeader
        eyebrow="Inventario · Maíz"
        title="Kardex de maíz"
        description="Movimientos generados por las recepciones aceptadas. El saldo se calcula automáticamente y el historial no se edita."
        actions={
          <>
            {user && canExportKardex(user.rol) && (
              <button
                type="button"
                className="button button-secondary"
                onClick={handleExport}
                data-testid="btn-exportar-kardex"
              >
                <Download size={17} />
                Exportar CSV
              </button>
            )}
            <button
              type="button"
              className="button button-secondary"
              onClick={() => void load(true)}
              disabled={refreshing}
            >
              {refreshing ? <InlineSpinner /> : <RefreshCw size={17} />}
              Actualizar
            </button>
          </>
        }
      />

      <div className="stats-grid stats-grid-3">
        <StatCard
          icon={Scale}
          label="Stock actual"
          value={formatKg(stockKg)}
          helper="Saldo disponible en planta"
        />
        <StatCard
          icon={BookOpen}
          label="Movimientos del libro"
          value={movimientos.length}
          helper="Entradas y ajustes"
          tone="blue"
        />
        <StatCard
          icon={ArrowDownLeft}
          label="Último movimiento"
          value={lastMovement ? formatKg(lastMovement.cantidadKg) : '—'}
          helper={lastMovement ? formatDateTime(lastMovement.fechaHora) : 'Sin movimientos'}
          tone="gold"
        />
      </div>

      <section className="panel">
        <div className="panel-head panel-head-wrap">
          <div>
            <h2>Libro de movimientos</h2>
            <p>Documento de origen, cantidad, saldo y usuario responsable.</p>
          </div>
          <Badge tone="neutral">Maíz en grano</Badge>
        </div>

        {movimientos.length === 0 ? (
          <EmptyState
            icon={BookOpen}
            title="El kardex está vacío"
            description="Acepte una recepción para generar la primera entrada de maíz."
          />
        ) : (
          <>
          <div className="table-wrap">
            <table className="data-table po01-table kardex-table" data-testid="tabla-kardex">
              <thead>
                <tr>
                  <th>Fecha y hora</th>
                  <th>Movimiento</th>
                  <th>Documento</th>
                  <th>Cantidad</th>
                  <th>Saldo</th>
                  <th>Usuario</th>
                </tr>
              </thead>
              <tbody>
                {movimientos.map((row) => (
                  <tr key={row.idMovimiento}>
                    <td>{formatDateTime(row.fechaHora)}</td>
                    <td>
                      <Badge tone={movementTone[row.tipoMovimiento]} dot>
                        {movementLabel[row.tipoMovimiento]}
                      </Badge>
                      <span className="po01-mobile-meta">{formatDateTime(row.fechaHora)}</span>
                      <span className="po01-mobile-meta">{origen(row)}</span>
                      <span className="po01-mobile-meta">{displayUser(row.usuario)}</span>
                    </td>
                    <td>{origen(row)}</td>
                    <td>
                      <strong className="ledger-amount">
                        {row.tipoMovimiento === 'ENTRADA' ? '+' : '−'}
                        {formatKg(row.cantidadKg)}
                      </strong>
                    </td>
                    <td className="ledger-balance">{formatKg(row.saldoKg)}</td>
                    <td>{displayUser(row.usuario)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          </>
        )}
      </section>

      <p className="po01-footnote">
        El kardex es inmutable. Si se requiere una corrección, se registra un ajuste
        con motivo y usuario responsable.
        {user && !canExportKardex(user.rol)
          ? ' Su rol puede consultar, pero no exportar el libro.'
          : ''}
      </p>
    </div>
  )
}
