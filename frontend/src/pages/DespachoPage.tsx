import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  AlertTriangle,
  ArrowRight,
  CheckCircle2,
  ClipboardList,
  MapPin,
  Plus,
  Search,
  ShoppingCart,
  Truck,
  X,
} from 'lucide-react'
import { api } from '../api'
import type {
  Cliente,
  Despacho,
  EstadoDespacho,
  EstadoPedido,
  Pedido,
  Producto,
  TipoEntrega,
} from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { Badge } from '../components/Badge'
import { EmptyState } from '../components/EmptyState'
import { InlineSpinner, LoadingBlock } from '../components/Loading'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { DespachoBadge, PedidoBadge } from '../components/StateBadge'
import { useToast } from '../components/Toast'
import {
  formatDateTime,
  formatKg,
  formatNumber,
  formatSacos,
} from '../lib/format'
import { canConfirmDispatch, canManageOrders } from '../lib/permissions'

type DispatchTab = 'pedidos' | 'despachos'

interface PedidoLineForm {
  id_producto: number
  cantidad_sacos: string
}

interface PedidoFormState {
  id_cliente: number
  observacion: string
  items: PedidoLineForm[]
}

interface DespachoFormState {
  id_pedido: number
  tipo_entrega: TipoEntrega
  destino: string
}

const pedidoStatusFilters: Array<{ value: 'TODOS' | EstadoPedido; label: string }> = [
  { value: 'TODOS', label: 'Todos' },
  { value: 'PENDIENTE', label: 'Pendientes' },
  { value: 'ATENDIDO', label: 'Atendidos' },
]

const despachoStatusFilters: Array<{ value: 'TODOS' | EstadoDespacho; label: string }> = [
  { value: 'TODOS', label: 'Todos' },
  { value: 'BORRADOR', label: 'Borradores' },
  { value: 'CONFIRMADO', label: 'Confirmados' },
]

function itemsKg(pedido: Pedido): number {
  return pedido.items.reduce((total, item) => total + item.cantidad_kg, 0)
}

function itemsSacos(pedido: Pedido): number {
  return pedido.items.reduce((total, item) => total + item.cantidad_sacos, 0)
}

export function DespachoPage() {
  const { user } = useAuth()
  const { notify, notifyError } = useToast()
  const [pedidos, setPedidos] = useState<Pedido[]>([])
  const [despachos, setDespachos] = useState<Despacho[]>([])
  const [productos, setProductos] = useState<Producto[]>([])
  const [clientes, setClientes] = useState<Cliente[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [tab, setTab] = useState<DispatchTab>('pedidos')
  const [query, setQuery] = useState('')
  const [pedidoStatus, setPedidoStatus] = useState<'TODOS' | EstadoPedido>('TODOS')
  const [despachoStatus, setDespachoStatus] = useState<'TODOS' | EstadoDespacho>('TODOS')
  const [pedidoOpen, setPedidoOpen] = useState(false)
  const [pedidoForm, setPedidoForm] = useState<PedidoFormState>({
    id_cliente: 0,
    observacion: '',
    items: [{ id_producto: 0, cantidad_sacos: '1' }],
  })
  const [pedidoSaving, setPedidoSaving] = useState(false)
  const [pedidoError, setPedidoError] = useState('')
  const [despachoOpen, setDespachoOpen] = useState(false)
  const [despachoForm, setDespachoForm] = useState<DespachoFormState>({
    id_pedido: 0,
    tipo_entrega: 'RECOJO_PLANTA',
    destino: '',
  })
  const [despachoSaving, setDespachoSaving] = useState(false)
  const [despachoError, setDespachoError] = useState('')
  const [confirmingId, setConfirmingId] = useState<number | null>(null)
  const [detailPedido, setDetailPedido] = useState<Pedido | null>(null)
  const [detailDespacho, setDetailDespacho] = useState<Despacho | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [orderRows, dispatchRows, productRows, clientRows] = await Promise.all([
        api.getPedidos(),
        api.getDespachos(),
        api.getProductos(),
        api.getClientes(),
      ])
      setPedidos(orderRows)
      setDespachos(dispatchRows)
      setProductos(productRows)
      setClientes(clientRows)
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : 'No se pudieron cargar los pedidos y despachos.',
      )
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const activeDispatchFor = useCallback(
    (idPedido: number) =>
      despachos.find(
        (row) =>
          row.id_pedido === idPedido &&
          (row.estado === 'BORRADOR' || row.estado === 'CONFIRMADO'),
      ),
    [despachos],
  )

  const eligiblePedidos = useMemo(
    () =>
      pedidos.filter(
        (pedido) =>
          pedido.estado === 'PENDIENTE' && !activeDispatchFor(pedido.id_pedido),
      ),
    [pedidos, activeDispatchFor],
  )

  const filteredPedidos = useMemo(() => {
    const term = query.trim().toLowerCase()
    return pedidos.filter((pedido) => {
      const matchesStatus =
        pedidoStatus === 'TODOS' || pedido.estado === pedidoStatus
      const matchesTerm =
        !term ||
        pedido.cliente_nombre.toLowerCase().includes(term) ||
        String(pedido.id_pedido).includes(term) ||
        pedido.items.some((item) =>
          item.producto_nombre.toLowerCase().includes(term),
        )
      return matchesStatus && matchesTerm
    })
  }, [pedidos, pedidoStatus, query])

  const filteredDespachos = useMemo(() => {
    const term = query.trim().toLowerCase()
    return despachos.filter((despacho) => {
      const matchesStatus =
        despachoStatus === 'TODOS' || despacho.estado === despachoStatus
      const matchesTerm =
        !term ||
        despacho.cliente_nombre.toLowerCase().includes(term) ||
        String(despacho.id_despacho).includes(term) ||
        String(despacho.id_pedido).includes(term) ||
        (despacho.destino ?? '').toLowerCase().includes(term)
      return matchesStatus && matchesTerm
    })
  }, [despachos, despachoStatus, query])

  if (!user) return null

  const canManage = canManageOrders(user.rol)
  const canConfirm = canConfirmDispatch(user.rol)
  const pendingOrders = pedidos.filter((row) => row.estado === 'PENDIENTE').length
  const draftDispatches = despachos.filter((row) => row.estado === 'BORRADOR').length
  const confirmedDispatches = despachos.filter(
    (row) => row.estado === 'CONFIRMADO',
  )
  const dispatchedKg = confirmedDispatches.reduce(
    (total, row) => total + row.items.reduce((sum, item) => sum + item.cantidad_kg, 0),
    0,
  )
  const pedidoTotalKg = pedidoForm.items.reduce(
    (total, line) => total + (Number(line.cantidad_sacos) || 0) * 40,
    0,
  )
  const pedidoTotalSacos = pedidoForm.items.reduce(
    (total, line) => total + (Number(line.cantidad_sacos) || 0),
    0,
  )
  const dispatchPedido = pedidos.find(
    (row) => row.id_pedido === despachoForm.id_pedido,
  )
  const dispatchIssues =
    dispatchPedido?.items
      .map((item) => {
        const product = productos.find(
          (row) => row.id_producto === item.id_producto,
        )
        return {
          item,
          available: product?.stock_kg ?? 0,
        }
      })
      .filter((row) => row.available < row.item.cantidad_kg) ?? []

  const openPedido = () => {
    const firstProduct = productos[0]
    setPedidoForm({
      id_cliente: clientes[0]?.id_cliente ?? 0,
      observacion: '',
      items: [
        {
          id_producto: firstProduct?.id_producto ?? 0,
          cantidad_sacos: '1',
        },
      ],
    })
    setPedidoError('')
    setPedidoOpen(true)
  }

  const openDespacho = (idPedido?: number) => {
    const candidate =
      eligiblePedidos.find((row) => row.id_pedido === idPedido) ??
      eligiblePedidos[0]
    if (!candidate) {
      notify('No hay pedidos pendientes sin despacho para preparar.', 'info')
      return
    }
    setDespachoForm({
      id_pedido: candidate.id_pedido,
      tipo_entrega: 'RECOJO_PLANTA',
      destino: '',
    })
    setDespachoError('')
    setDespachoOpen(true)
  }

  const addLine = () => {
    setPedidoForm((current) => ({
      ...current,
      items: [
        ...current.items,
        {
          id_producto: productos[0]?.id_producto ?? 0,
          cantidad_sacos: '1',
        },
      ],
    }))
  }

  const updateLine = (
    index: number,
    field: keyof PedidoLineForm,
    value: string | number,
  ) => {
    setPedidoForm((current) => ({
      ...current,
      items: current.items.map((line, lineIndex) =>
        lineIndex === index ? { ...line, [field]: value } : line,
      ),
    }))
  }

  const removeLine = (index: number) => {
    setPedidoForm((current) => ({
      ...current,
      items: current.items.filter((_, lineIndex) => lineIndex !== index),
    }))
  }

  const handleCreatePedido = async (event: FormEvent) => {
    event.preventDefault()
    setPedidoError('')
    if (!pedidoForm.id_cliente) {
      setPedidoError('Seleccione un cliente.')
      return
    }
    if (!pedidoForm.items.length) {
      setPedidoError('Agregue al menos un producto.')
      return
    }
    const items = pedidoForm.items.map((line) => ({
      id_producto: line.id_producto,
      cantidad_sacos: Number(line.cantidad_sacos),
    }))
    if (
      items.some(
        (item) =>
          !item.id_producto ||
          !Number.isFinite(item.cantidad_sacos) ||
          item.cantidad_sacos <= 0,
      )
    ) {
      setPedidoError('Complete producto y cantidad de sacos en cada línea.')
      return
    }
    setPedidoSaving(true)
    try {
      const created = await api.crearPedido({
        id_cliente: pedidoForm.id_cliente,
        observacion: pedidoForm.observacion,
        items,
      })
      setPedidos((current) => [created, ...current])
      setPedidoOpen(false)
      setTab('pedidos')
      notify('Pedido registrado. Ya puede preparar el despacho.')
    } catch (saveError) {
      const message =
        saveError instanceof Error ? saveError.message : 'No se pudo crear el pedido.'
      setPedidoError(message)
      notifyError(saveError)
    } finally {
      setPedidoSaving(false)
    }
  }

  const handleCreateDespacho = async (event: FormEvent) => {
    event.preventDefault()
    setDespachoError('')
    if (!despachoForm.id_pedido) {
      setDespachoError('Seleccione un pedido.')
      return
    }
    if (despachoForm.tipo_entrega === 'REPARTO' && !despachoForm.destino.trim()) {
      setDespachoError('Ingrese el destino del reparto.')
      return
    }
    setDespachoSaving(true)
    try {
      const created = await api.crearDespacho({
        id_pedido: despachoForm.id_pedido,
        tipo_entrega: despachoForm.tipo_entrega,
        destino: despachoForm.destino,
      })
      setDespachos((current) => [created, ...current])
      setDespachoOpen(false)
      setTab('despachos')
      notify('Despacho creado en borrador. Confirme la salida para descontar el stock.')
    } catch (saveError) {
      const message =
        saveError instanceof Error
          ? saveError.message
          : 'No se pudo crear el despacho.'
      setDespachoError(message)
      notifyError(saveError)
    } finally {
      setDespachoSaving(false)
    }
  }

  const handleConfirmDespacho = async (despacho: Despacho) => {
    const confirmed = window.confirm(
      `¿Confirmar la salida del despacho #${despacho.id_despacho} para ${despacho.cliente_nombre}? Se descontará el stock.`,
    )
    if (!confirmed) return
    setConfirmingId(despacho.id_despacho)
    try {
      await api.confirmarDespacho(despacho.id_despacho)
      await load()
      notify('Despacho confirmado. El stock y el pedido fueron actualizados.')
    } catch (confirmError) {
      notifyError(confirmError)
    } finally {
      setConfirmingId(null)
    }
  }

  return (
    <div className="stack-lg">
      <PageHeader
        eyebrow="PO-05 · Venta y despacho"
        title="Venta y despacho"
        description="Registre pedidos, prepare la entrega y confirme la salida con validación de stock."
        actions={
          canManage && (
            <>
              <button
                type="button"
                className="button button-secondary"
                onClick={() => openDespacho()}
                disabled={!eligiblePedidos.length}
              >
                <Truck size={17} />
                Nuevo despacho
              </button>
              <button
                type="button"
                className="button button-primary"
                onClick={openPedido}
                disabled={!productos.length || !clientes.length}
              >
                <Plus size={18} />
                Nuevo pedido
              </button>
            </>
          )
        }
      />

      <section className="mini-stats">
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-gold"><ShoppingCart size={18} /></span>
          <div>
            <strong>{pendingOrders}</strong>
            <span>Pedidos pendientes</span>
          </div>
        </div>
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-blue"><ClipboardList size={18} /></span>
          <div>
            <strong>{draftDispatches}</strong>
            <span>Despachos en borrador</span>
          </div>
        </div>
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-green"><CheckCircle2 size={18} /></span>
          <div>
            <strong>{confirmedDispatches.length}</strong>
            <span>Despachos confirmados</span>
          </div>
        </div>
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-green"><Truck size={18} /></span>
          <div>
            <strong>{formatNumber(dispatchedKg)} kg</strong>
            <span>Despachados</span>
          </div>
        </div>
      </section>

      {error && (
        <div className="inline-alert inline-alert-error">
          <AlertTriangle size={18} />
          {error}
        </div>
      )}

      <div className="flex-toolbar">
        <div className="segmented" role="tablist" aria-label="Pedidos y despachos">
          <button
            type="button"
            role="tab"
            aria-selected={tab === 'pedidos'}
            className={`segmented-item ${tab === 'pedidos' ? 'segmented-active' : ''}`}
            onClick={() => setTab('pedidos')}
          >
            <ClipboardList size={17} />
            Pedidos
            <span className="segmented-count">{pedidos.length}</span>
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={tab === 'despachos'}
            className={`segmented-item ${tab === 'despachos' ? 'segmented-active' : ''}`}
            onClick={() => setTab('despachos')}
          >
            <Truck size={17} />
            Despachos
            <span className="segmented-count">{despachos.length}</span>
          </button>
        </div>
        <div className="search-box">
          <Search size={17} aria-hidden="true" />
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={
              tab === 'pedidos' ? 'Buscar pedido o cliente' : 'Buscar despacho o destino'
            }
            aria-label="Buscar"
          />
        </div>
      </div>

      {loading ? (
        <LoadingBlock label="Cargando pedidos y despachos…" />
      ) : tab === 'pedidos' ? (
        <section className="panel">
          <header className="panel-head panel-head-wrap">
            <div>
              <h2>Pedidos de clientes</h2>
              <p>Disponibilidad validada al registrar el pedido y al confirmar la salida.</p>
            </div>
            <div className="filter-pills" role="group" aria-label="Filtrar pedidos">
              {pedidoStatusFilters.map((filter) => (
                <button
                  key={filter.value}
                  type="button"
                  className={`filter-pill ${pedidoStatus === filter.value ? 'filter-pill-active' : ''}`}
                  onClick={() => setPedidoStatus(filter.value)}
                >
                  {filter.label}
                </button>
              ))}
            </div>
          </header>

          {filteredPedidos.length === 0 ? (
            <EmptyState
              icon={ClipboardList}
              title="Sin pedidos para mostrar"
              description="Registre el primer pedido de un cliente para iniciar el flujo de despacho."
              action={
                canManage && (
                  <button type="button" className="button button-primary" onClick={openPedido}>
                    <Plus size={17} />
                    Registrar pedido
                  </button>
                )
              }
            />
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Pedido</th>
                    <th>Cliente</th>
                    <th>Contenido</th>
                    <th>Estado</th>
                    <th>Despacho</th>
                    <th className="align-right">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPedidos.map((pedido) => {
                    const dispatch = activeDispatchFor(pedido.id_pedido)
                    return (
                      <tr key={pedido.id_pedido}>
                        <td>
                          <div className="cell-main">
                            <strong>#{String(pedido.id_pedido).padStart(3, '0')}</strong>
                            <span>{formatDateTime(pedido.fecha_hora)}</span>
                          </div>
                        </td>
                        <td>
                          <div className="cell-main">
                            <strong>{pedido.cliente_nombre}</strong>
                            <span>{pedido.observacion ?? 'Sin observación'}</span>
                          </div>
                        </td>
                        <td>
                          <div className="cell-main">
                            <strong>{formatSacos(itemsSacos(pedido))}</strong>
                            <span>{formatKg(itemsKg(pedido))}</span>
                          </div>
                        </td>
                        <td><PedidoBadge estado={pedido.estado} /></td>
                        <td>
                          {dispatch ? (
                            <div className="cell-main">
                              <DespachoBadge estado={dispatch.estado} />
                              <span>#{String(dispatch.id_despacho).padStart(3, '0')}</span>
                            </div>
                          ) : (
                            <span className="muted">Sin despacho</span>
                          )}
                        </td>
                        <td className="align-right">
                          <div className="row-actions">
                            {pedido.estado === 'PENDIENTE' && !dispatch && canManage && (
                              <button
                                type="button"
                                className="button button-small button-primary"
                                onClick={() => openDespacho(pedido.id_pedido)}
                              >
                                Crear despacho
                                <ArrowRight size={15} />
                              </button>
                            )}
                            {dispatch && (
                              <button
                                type="button"
                                className="button button-small button-ghost"
                                onClick={() => {
                                  setTab('despachos')
                                  setDetailDespacho(dispatch)
                                }}
                              >
                                Ver despacho
                              </button>
                            )}
                            {pedido.estado === 'ATENDIDO' && (
                              <button
                                type="button"
                                className="button button-small button-ghost"
                                onClick={() => setDetailPedido(pedido)}
                              >
                                Ver pedido
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      ) : (
        <section className="panel">
          <header className="panel-head panel-head-wrap">
            <div>
              <h2>Despachos</h2>
              <p>Confirme la salida para descontar stock y cerrar el pedido.</p>
            </div>
            <div className="filter-pills" role="group" aria-label="Filtrar despachos">
              {despachoStatusFilters.map((filter) => (
                <button
                  key={filter.value}
                  type="button"
                  className={`filter-pill ${despachoStatus === filter.value ? 'filter-pill-active' : ''}`}
                  onClick={() => setDespachoStatus(filter.value)}
                >
                  {filter.label}
                </button>
              ))}
            </div>
          </header>

          {filteredDespachos.length === 0 ? (
            <EmptyState
              icon={Truck}
              title="Sin despachos para mostrar"
              description="Cree un despacho a partir de un pedido pendiente para preparar la entrega."
              action={
                canManage && eligiblePedidos.length > 0 ? (
                  <button
                    type="button"
                    className="button button-primary"
                    onClick={() => openDespacho()}
                  >
                    <Plus size={17} />
                    Nuevo despacho
                  </button>
                ) : undefined
              }
            />
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Despacho</th>
                    <th>Pedido / cliente</th>
                    <th>Entrega</th>
                    <th>Contenido</th>
                    <th>Estado</th>
                    <th className="align-right">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredDespachos.map((despacho) => {
                    const pedido = pedidos.find(
                      (row) => row.id_pedido === despacho.id_pedido,
                    )
                    return (
                      <tr key={despacho.id_despacho}>
                        <td>
                          <div className="cell-main">
                            <strong>#{String(despacho.id_despacho).padStart(3, '0')}</strong>
                            <span>{formatDateTime(despacho.fecha_hora)}</span>
                          </div>
                        </td>
                        <td>
                          <div className="cell-main">
                            <strong>{despacho.cliente_nombre}</strong>
                            <span>Pedido #{String(despacho.id_pedido).padStart(3, '0')}</span>
                          </div>
                        </td>
                        <td>
                          <div className="cell-main">
                            <Badge tone={despacho.tipo_entrega === 'REPARTO' ? 'info' : 'neutral'}>
                              {despacho.tipo_entrega === 'REPARTO' ? 'Reparto' : 'Recojo en planta'}
                            </Badge>
                            <span>
                              {despacho.destino ? (
                                <>
                                  <MapPin size={12} /> {despacho.destino}
                                </>
                              ) : (
                                'Entrega en planta'
                              )}
                            </span>
                          </div>
                        </td>
                        <td>
                          <div className="cell-main">
                            <strong>
                              {formatSacos(
                                despacho.items.reduce(
                                  (total, item) => total + item.cantidad_sacos,
                                  0,
                                ),
                              )}
                            </strong>
                            <span>
                              {formatKg(
                                despacho.items.reduce(
                                  (total, item) => total + item.cantidad_kg,
                                  0,
                                ),
                              )}
                            </span>
                          </div>
                        </td>
                        <td><DespachoBadge estado={despacho.estado} /></td>
                        <td className="align-right">
                          <div className="row-actions">
                            {despacho.estado === 'BORRADOR' && canConfirm && (
                              <button
                                type="button"
                                className="button button-small button-primary"
                                onClick={() => void handleConfirmDespacho(despacho)}
                                disabled={confirmingId === despacho.id_despacho}
                              >
                                {confirmingId === despacho.id_despacho ? (
                                  <InlineSpinner />
                                ) : (
                                  <CheckCircle2 size={15} />
                                )}
                                Confirmar salida
                              </button>
                            )}
                            <button
                              type="button"
                              className="button button-small button-ghost"
                              onClick={() => setDetailDespacho(despacho)}
                            >
                              Ver detalle
                            </button>
                            {pedido && (
                              <button
                                type="button"
                                className="button button-small button-ghost"
                                onClick={() => setDetailPedido(pedido)}
                              >
                                Ver pedido
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      <Modal
        open={pedidoOpen}
        title="Registrar nuevo pedido"
        subtitle="El pedido se valida contra el stock disponible al momento de registrarlo."
        onClose={() => setPedidoOpen(false)}
        size="lg"
        footer={
          <>
            <button
              type="button"
              className="button button-secondary"
              onClick={() => setPedidoOpen(false)}
              disabled={pedidoSaving}
            >
              Cancelar
            </button>
            <button
              type="submit"
              form="nuevo-pedido-form"
              className="button button-primary"
              disabled={pedidoSaving}
            >
              {pedidoSaving ? <InlineSpinner /> : <Plus size={17} />}
              {pedidoSaving ? 'Registrando…' : 'Registrar pedido'}
            </button>
          </>
        }
      >
        <form id="nuevo-pedido-form" className="form-grid" onSubmit={handleCreatePedido}>
          <label className="field">
            <span>Cliente</span>
            <select
              value={pedidoForm.id_cliente}
              onChange={(event) =>
                setPedidoForm((current) => ({
                  ...current,
                  id_cliente: Number(event.target.value),
                }))
              }
            >
              {clientes.map((client) => (
                <option key={client.id_cliente} value={client.id_cliente}>
                  {client.nombre_razon_social} · {client.zona ?? 'Sin zona'}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Observación</span>
            <input
              type="text"
              value={pedidoForm.observacion}
              onChange={(event) =>
                setPedidoForm((current) => ({
                  ...current,
                  observacion: event.target.value,
                }))
              }
              placeholder="Entrega, horario o referencia"
            />
          </label>

          <div className="form-span-2">
            <div className="line-editor-head">
              <div>
                <strong>Productos del pedido</strong>
                <span>Seleccione producto y cantidad en sacos de 40 kg.</span>
              </div>
              <button type="button" className="button button-small button-secondary" onClick={addLine}>
                <Plus size={15} />
                Agregar línea
              </button>
            </div>

            <div className="line-editor">
              {pedidoForm.items.map((line, index) => {
                const product = productos.find(
                  (row) => row.id_producto === line.id_producto,
                )
                const requested = (Number(line.cantidad_sacos) || 0) * 40
                const insufficient = product ? requested > product.stock_kg : false
                return (
                  <div key={index} className="line-editor-row">
                    <label className="field">
                      <span>Producto</span>
                      <select
                        value={line.id_producto}
                        onChange={(event) =>
                          updateLine(index, 'id_producto', Number(event.target.value))
                        }
                      >
                        {productos.map((row) => (
                          <option key={row.id_producto} value={row.id_producto}>
                            {row.nombre} · {formatKg(row.stock_kg)} disponibles
                          </option>
                        ))}
                      </select>
                    </label>
                    <label className="field">
                      <span>Sacos</span>
                      <input
                        type="number"
                        min="1"
                        step="1"
                        value={line.cantidad_sacos}
                        onChange={(event) =>
                          updateLine(index, 'cantidad_sacos', event.target.value)
                        }
                      />
                    </label>
                    <div className="line-editor-total">
                      <span>Total</span>
                      <strong className={insufficient ? 'text-danger' : ''}>
                        {formatKg(requested)}
                      </strong>
                    </div>
                    <button
                      type="button"
                      className="icon-button line-remove"
                      onClick={() => removeLine(index)}
                      disabled={pedidoForm.items.length === 1}
                      aria-label="Quitar línea"
                    >
                      <X size={17} />
                    </button>
                    {insufficient && (
                      <p className="line-warning">
                        Stock insuficiente: disponible {formatKg(product?.stock_kg ?? 0)}.
                      </p>
                    )}
                  </div>
                )
              })}
            </div>
          </div>

          <div className="order-total form-span-2">
            <span>
              {formatSacos(pedidoTotalSacos)} · {formatKg(pedidoTotalKg)}
            </span>
            <strong>Total del pedido</strong>
          </div>

          {pedidoError && (
            <p className="form-error form-span-2" role="alert">
              {pedidoError}
            </p>
          )}
        </form>
      </Modal>

      <Modal
        open={despachoOpen}
        title="Preparar despacho"
        subtitle="Seleccione un pedido pendiente. El stock se valida al confirmar la salida."
        onClose={() => setDespachoOpen(false)}
        size="lg"
        footer={
          <>
            <button
              type="button"
              className="button button-secondary"
              onClick={() => setDespachoOpen(false)}
              disabled={despachoSaving}
            >
              Cancelar
            </button>
            <button
              type="submit"
              form="nuevo-despacho-form"
              className="button button-primary"
              disabled={despachoSaving}
            >
              {despachoSaving ? <InlineSpinner /> : <Truck size={17} />}
              {despachoSaving ? 'Creando…' : 'Crear despacho'}
            </button>
          </>
        }
      >
        <form id="nuevo-despacho-form" className="form-grid" onSubmit={handleCreateDespacho}>
          <label className="field form-span-2">
            <span>Pedido pendiente</span>
            <select
              value={despachoForm.id_pedido}
              onChange={(event) =>
                setDespachoForm((current) => ({
                  ...current,
                  id_pedido: Number(event.target.value),
                }))
              }
            >
              {eligiblePedidos.map((pedido) => (
                <option key={pedido.id_pedido} value={pedido.id_pedido}>
                  #{String(pedido.id_pedido).padStart(3, '0')} · {pedido.cliente_nombre} ·{' '}
                  {formatKg(itemsKg(pedido))}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Tipo de entrega</span>
            <select
              value={despachoForm.tipo_entrega}
              onChange={(event) =>
                setDespachoForm((current) => ({
                  ...current,
                  tipo_entrega: event.target.value as TipoEntrega,
                  destino: event.target.value === 'RECOJO_PLANTA' ? '' : current.destino,
                }))
              }
            >
              <option value="RECOJO_PLANTA">Recojo en planta</option>
              <option value="REPARTO">Reparto</option>
            </select>
          </label>

          <label className="field">
            <span>Destino</span>
            <input
              type="text"
              value={despachoForm.destino}
              onChange={(event) =>
                setDespachoForm((current) => ({
                  ...current,
                  destino: event.target.value,
                }))
              }
              placeholder={
                despachoForm.tipo_entrega === 'REPARTO'
                  ? 'Dirección o zona de entrega'
                  : 'No aplica para recojo en planta'
              }
              disabled={despachoForm.tipo_entrega === 'RECOJO_PLANTA'}
            />
          </label>

          {dispatchPedido && (
            <div className="form-span-2 dispatch-summary">
              <div className="dispatch-summary-head">
                <div>
                  <strong>Pedido #{String(dispatchPedido.id_pedido).padStart(3, '0')}</strong>
                  <span>{dispatchPedido.cliente_nombre}</span>
                </div>
                <Badge tone="neutral">
                  {formatSacos(itemsSacos(dispatchPedido))}
                </Badge>
              </div>
              <div className="dispatch-lines">
                {dispatchPedido.items.map((item) => {
                  const product = productos.find(
                    (row) => row.id_producto === item.id_producto,
                  )
                  const insufficient = (product?.stock_kg ?? 0) < item.cantidad_kg
                  return (
                    <div key={item.id_producto} className="dispatch-line">
                      <div>
                        <strong>{item.producto_nombre}</strong>
                        <span>
                          Requerido {formatKg(item.cantidad_kg)} · Disponible{' '}
                          {formatKg(product?.stock_kg ?? 0)}
                        </span>
                      </div>
                      {insufficient && <Badge tone="danger">Falta stock</Badge>}
                    </div>
                  )
                })}
              </div>
              {dispatchIssues.length > 0 && (
                <p className="inline-alert inline-alert-warning">
                  <AlertTriangle size={16} />
                  El pedido no tiene stock suficiente para confirmar la salida. Puede
                  crear el borrador y producir antes de confirmar.
                </p>
              )}
            </div>
          )}

          {despachoError && (
            <p className="form-error form-span-2" role="alert">
              {despachoError}
            </p>
          )}
        </form>
      </Modal>

      <Modal
        open={Boolean(detailPedido)}
        title={`Pedido #${String(detailPedido?.id_pedido ?? '').padStart(3, '0')}`}
        subtitle={detailPedido?.cliente_nombre}
        onClose={() => setDetailPedido(null)}
        size="md"
        footer={
          <button
            type="button"
            className="button button-secondary"
            onClick={() => setDetailPedido(null)}
          >
            Cerrar
          </button>
        }
      >
        {detailPedido && (
          <div className="stack-md">
            <div className="detail-grid">
              <div>
                <span>Estado</span>
                <PedidoBadge estado={detailPedido.estado} />
              </div>
              <div>
                <span>Fecha</span>
                <strong>{formatDateTime(detailPedido.fecha_hora)}</strong>
              </div>
              <div>
                <span>Total</span>
                <strong>{formatKg(itemsKg(detailPedido))}</strong>
              </div>
              <div>
                <span>Observación</span>
                <strong>{detailPedido.observacion ?? '—'}</strong>
              </div>
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th className="align-right">Sacos</th>
                    <th className="align-right">Kg</th>
                  </tr>
                </thead>
                <tbody>
                  {detailPedido.items.map((item) => (
                    <tr key={item.id_producto}>
                      <td>{item.producto_nombre}</td>
                      <td className="align-right numeric">{item.cantidad_sacos}</td>
                      <td className="align-right numeric">{formatKg(item.cantidad_kg)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </Modal>

      <Modal
        open={Boolean(detailDespacho)}
        title={`Despacho #${String(detailDespacho?.id_despacho ?? '').padStart(3, '0')}`}
        subtitle={detailDespacho?.cliente_nombre}
        onClose={() => setDetailDespacho(null)}
        size="md"
        footer={
          <button
            type="button"
            className="button button-secondary"
            onClick={() => setDetailDespacho(null)}
          >
            Cerrar
          </button>
        }
      >
        {detailDespacho && (
          <div className="stack-md">
            <div className="detail-grid">
              <div>
                <span>Estado</span>
                <DespachoBadge estado={detailDespacho.estado} />
              </div>
              <div>
                <span>Pedido</span>
                <strong>#{String(detailDespacho.id_pedido).padStart(3, '0')}</strong>
              </div>
              <div>
                <span>Entrega</span>
                <strong>
                  {detailDespacho.tipo_entrega === 'REPARTO'
                    ? 'Reparto'
                    : 'Recojo en planta'}
                </strong>
              </div>
              <div>
                <span>Destino</span>
                <strong>{detailDespacho.destino ?? 'Planta Moliselva'}</strong>
              </div>
              <div>
                <span>Responsable</span>
                <strong>{detailDespacho.usuario_nombre}</strong>
              </div>
              <div>
                <span>Fecha</span>
                <strong>{formatDateTime(detailDespacho.fecha_hora)}</strong>
              </div>
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th className="align-right">Sacos</th>
                    <th className="align-right">Kg</th>
                  </tr>
                </thead>
                <tbody>
                  {detailDespacho.items.map((item) => (
                    <tr key={item.id_producto}>
                      <td>{item.producto_nombre}</td>
                      <td className="align-right numeric">{item.cantidad_sacos}</td>
                      <td className="align-right numeric">{formatKg(item.cantidad_kg)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}
