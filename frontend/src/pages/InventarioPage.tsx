import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  AlertTriangle,
  ArrowDownLeft,
  ArrowUpRight,
  Boxes,
  Download,
  Layers,
  Package,
  RefreshCw,
  Scale,
  Search,
  Warehouse,
} from 'lucide-react'
import { api } from '../api'
import type { Movimiento, StockResumen } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { Badge } from '../components/Badge'
import { EmptyState } from '../components/EmptyState'
import { InlineSpinner, LoadingBlock } from '../components/Loading'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { useToast } from '../components/Toast'
import {
  downloadCsv,
  formatDateTime,
  formatKg,
  formatNumber,
  formatSacos,
} from '../lib/format'

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

type InventoryTab = 'stock' | 'kardex'
type MovementFilter = 'TODOS' | Movimiento['tipo_movimiento']

export function InventarioPage() {
  const { user } = useAuth()
  const { notifyError } = useToast()
  const [stock, setStock] = useState<StockResumen | null>(null)
  const [kardex, setKardex] = useState<Movimiento[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState('')
  const [tab, setTab] = useState<InventoryTab>('stock')
  const [stockQuery, setStockQuery] = useState('')
  const [kardexQuery, setKardexQuery] = useState('')
  const [movementFilter, setMovementFilter] = useState<MovementFilter>('TODOS')

  const load = useCallback(async (refresh = false) => {
    if (refresh) setRefreshing(true)
    else setLoading(true)
    setError('')
    try {
      const [stockData, kardexRows] = await Promise.all([
        api.getStock(),
        api.getKardex(),
      ])
      setStock(stockData)
      setKardex(kardexRows)
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : 'No se pudo cargar el inventario.',
      )
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const filteredProducts = useMemo(() => {
    if (!stock) return []
    const term = stockQuery.trim().toLowerCase()
    return stock.productos.filter(
      (product) =>
        !term ||
        product.nombre.toLowerCase().includes(term) ||
        product.especie.toLowerCase().includes(term) ||
        product.fase.toLowerCase().includes(term),
    )
  }, [stock, stockQuery])

  const filteredInputs = useMemo(() => {
    if (!stock) return []
    const term = stockQuery.trim().toLowerCase()
    return stock.insumos.filter(
      (input) => !term || input.nombre.toLowerCase().includes(term),
    )
  }, [stock, stockQuery])

  const filteredKardex = useMemo(() => {
    const term = kardexQuery.trim().toLowerCase()
    return kardex.filter((movement) => {
      const matchesType =
        movementFilter === 'TODOS' ||
        movement.tipo_movimiento === movementFilter
      const matchesTerm =
        !term ||
        movement.item.toLowerCase().includes(term) ||
        (movement.referencia ?? '').toLowerCase().includes(term) ||
        movement.usuario_nombre.toLowerCase().includes(term)
      return matchesType && matchesTerm
    })
  }, [kardex, kardexQuery, movementFilter])

  if (!user) return null

  const maxProductStock = stock
    ? Math.max(...stock.productos.map((product) => product.stock_kg), 1)
    : 1
  const lowStock = stock
    ? stock.productos.filter((product) => product.stock_kg <= 80)
    : []

  const handleExport = () => {
    if (!stock) return
    const rows: Array<Array<string | number>> = [
      ['Tipo', 'Ítem', 'Especie / fase', 'Línea', 'Stock kg', 'Sacos'],
      ...stock.productos.map((product) => [
        'Producto',
        product.nombre,
        `${product.especie} / ${product.fase}`,
        product.linea_comercial ?? '—',
        product.stock_kg,
        Math.floor(product.stock_kg / 40),
      ]),
      ...stock.insumos.map((input) => [
        'Insumo',
        input.nombre,
        '—',
        '—',
        input.stock_kg,
        '—',
      ]),
    ]
    downloadCsv('inventario_moliselva.csv', rows)
  }

  if (loading && !stock) return <LoadingBlock label="Contando existencias…" />

  if (error && !stock) {
    return (
      <div className="panel error-panel">
        <AlertTriangle size={24} />
        <h2>No se pudo cargar el inventario</h2>
        <p>{error}</p>
        <button type="button" className="button button-primary" onClick={() => void load()}>
          Reintentar
        </button>
      </div>
    )
  }

  if (!stock) return null

  return (
    <div className="stack-lg">
      <PageHeader
        eyebrow="PO-04 · Inventario"
        title="Inventario en cuadrícula"
        description="Existencias de producto terminado e insumos, con kardex trazable por movimiento."
        actions={
          <>
            <button type="button" className="button button-secondary" onClick={handleExport}>
              <Download size={17} />
              Exportar CSV
            </button>
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

      {error && (
        <div className="inline-alert inline-alert-error">
          <AlertTriangle size={18} />
          {error}
        </div>
      )}

      <section className="stats-grid" aria-label="Resumen de inventario">
        <StatCard
          icon={Warehouse}
          label="Producto terminado"
          value={formatKg(stock.total_producto_kg)}
          helper={`${formatSacos(stock.total_sacos)} · 15 presentaciones`}
          tone="green"
        />
        <StatCard
          icon={Package}
          label="Insumos disponibles"
          value={formatNumber(stock.insumos.length, 0)}
          helper="Materias primas para producción"
          tone="gold"
        />
        <StatCard
          icon={Layers}
          label="Productos en catálogo"
          value={formatNumber(stock.productos.length, 0)}
          helper="Cerdo, pollo, cuy, conejo y vacuno"
          tone="blue"
        />
        <StatCard
          icon={AlertTriangle}
          label="Productos con stock bajo"
          value={formatNumber(lowStock.length, 0)}
          helper="Umbral de alerta: 80 kg"
          tone="red"
        />
      </section>

      <div className="segmented" role="tablist" aria-label="Vista de inventario">
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'stock'}
          className={`segmented-item ${tab === 'stock' ? 'segmented-active' : ''}`}
          onClick={() => setTab('stock')}
        >
          <Boxes size={17} />
          Existencias
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'kardex'}
          className={`segmented-item ${tab === 'kardex' ? 'segmented-active' : ''}`}
          onClick={() => setTab('kardex')}
        >
          <Scale size={17} />
          Kardex de movimientos
        </button>
      </div>

      {tab === 'stock' ? (
        <div className="stack-lg">
          <section className="panel">
            <header className="panel-head panel-head-wrap">
              <div>
                <h2>Producto terminado</h2>
                <p>Stock en kilogramos y equivalencia en sacos de 40 kg.</p>
              </div>
              <div className="search-box">
                <Search size={17} aria-hidden="true" />
                <input
                  type="search"
                  value={stockQuery}
                  onChange={(event) => setStockQuery(event.target.value)}
                  placeholder="Buscar producto, especie o fase"
                  aria-label="Buscar en inventario"
                />
              </div>
            </header>

            {filteredProducts.length === 0 ? (
              <EmptyState
                icon={Boxes}
                title="Sin productos"
                description="No hay productos que coincidan con la búsqueda."
              />
            ) : (
              <div className="inventory-grid">
                {filteredProducts.map((product) => {
                  const ratio = Math.max(
                    3,
                    Math.min(100, (product.stock_kg / maxProductStock) * 100),
                  )
                  const tone =
                    product.stock_kg <= 80
                      ? 'danger'
                      : product.stock_kg <= 200
                        ? 'warning'
                        : 'ok'
                  return (
                    <article key={product.id_producto} className="inventory-card">
                      <div className="inventory-card-head">
                        <div className={`inventory-icon inventory-icon-${tone}`}>
                          <Package size={19} />
                        </div>
                        {product.linea_comercial && (
                          <Badge tone="neutral">{product.linea_comercial}</Badge>
                        )}
                      </div>
                      <h3>{product.nombre}</h3>
                      <p>
                        {product.especie} · {product.fase}
                      </p>
                      <div className="inventory-figures">
                        <strong>{formatKg(product.stock_kg)}</strong>
                        <span>{formatSacos(Math.floor(product.stock_kg / 40))}</span>
                      </div>
                      <div className="stock-bar" aria-hidden="true">
                        <span
                          className={`stock-bar-fill stock-bar-${tone}`}
                          style={{ width: `${ratio}%` }}
                        />
                      </div>
                      <span className={`inventory-status inventory-status-${tone}`}>
                        {tone === 'danger'
                          ? 'Stock bajo'
                          : tone === 'warning'
                            ? 'Revisar stock'
                            : 'Disponible'}
                      </span>
                    </article>
                  )
                })}
              </div>
            )}
          </section>

          <section className="panel">
            <header className="panel-head">
              <div>
                <h2>Insumos</h2>
                <p>Materias primas disponibles para la producción.</p>
              </div>
            </header>
            {filteredInputs.length === 0 ? (
              <EmptyState
                icon={Package}
                title="Sin insumos"
                description="No hay insumos que coincidan con la búsqueda."
              />
            ) : (
              <div className="input-grid">
                {filteredInputs.map((input) => (
                  <div key={input.id_insumo} className="input-card">
                    <div className="input-card-icon">
                      <Warehouse size={19} />
                    </div>
                    <div>
                      <strong>{input.nombre}</strong>
                      <span>Unidad: {input.unidad_medida}</span>
                    </div>
                    <strong className="numeric">{formatKg(input.stock_kg)}</strong>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>
      ) : (
        <section className="panel">
          <header className="panel-head panel-head-wrap">
            <div>
              <h2>Kardex de movimientos</h2>
              <p>Trazabilidad de entradas, salidas y ajustes de inventario.</p>
            </div>
            <div className="table-tools">
              <div className="search-box">
                <Search size={17} aria-hidden="true" />
                <input
                  type="search"
                  value={kardexQuery}
                  onChange={(event) => setKardexQuery(event.target.value)}
                  placeholder="Buscar ítem, referencia o usuario"
                  aria-label="Buscar en kardex"
                />
              </div>
              <select
                className="compact-select"
                value={movementFilter}
                onChange={(event) =>
                  setMovementFilter(event.target.value as MovementFilter)
                }
                aria-label="Filtrar movimientos"
              >
                <option value="TODOS">Todos los movimientos</option>
                <option value="ENTRADA">Entradas</option>
                <option value="SALIDA">Salidas</option>
                <option value="AJUSTE_ENTRADA">Ajustes de entrada</option>
                <option value="AJUSTE_SALIDA">Ajustes de salida</option>
              </select>
            </div>
          </header>

          {filteredKardex.length === 0 ? (
            <EmptyState
              icon={Scale}
              title="Sin movimientos"
              description="No hay movimientos que coincidan con los filtros."
            />
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Movimiento</th>
                    <th>Ítem</th>
                    <th className="align-right">Cantidad</th>
                    <th className="align-right">Saldo</th>
                    <th>Referencia</th>
                    <th>Usuario</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredKardex.map((movement) => {
                    const incoming =
                      movement.tipo_movimiento === 'ENTRADA' ||
                      movement.tipo_movimiento === 'AJUSTE_ENTRADA'
                    return (
                      <tr key={movement.id_movimiento}>
                        <td className="muted nowrap">{formatDateTime(movement.fecha_hora)}</td>
                        <td>
                          <Badge tone={movementTone[movement.tipo_movimiento]} dot>
                            {incoming ? <ArrowDownLeft size={13} /> : <ArrowUpRight size={13} />}
                            {movementLabel[movement.tipo_movimiento]}
                          </Badge>
                        </td>
                        <td>
                          <div className="cell-main">
                            <strong>{movement.item}</strong>
                            <span>{movement.tipo_item === 'PRODUCTO' ? 'Producto' : 'Insumo'}</span>
                          </div>
                        </td>
                        <td className={`align-right numeric ${incoming ? 'text-success' : 'text-danger'}`}>
                          {incoming ? '+' : '−'}
                          {formatKg(movement.cantidad)}
                        </td>
                        <td className="align-right numeric">{formatKg(movement.saldo)}</td>
                        <td className="muted">{movement.referencia ?? '—'}</td>
                        <td className="muted">{movement.usuario_nombre}</td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}
    </div>
  )
}
