import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  AlertTriangle,
  CheckCircle2,
  Droplets,
  Plus,
  Scale,
  Search,
  ShieldCheck,
  Truck,
  XCircle,
} from 'lucide-react'
import { api } from '../api'
import type {
  EstadoRecepcion,
  Insumo,
  Proveedor,
  Recepcion,
} from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { Badge } from '../components/Badge'
import { EmptyState } from '../components/EmptyState'
import { InlineSpinner, LoadingBlock } from '../components/Loading'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { RecepcionBadge } from '../components/StateBadge'
import { useToast } from '../components/Toast'
import {
  formatDateTime,
  formatKg,
  formatNumber,
  humidityBand,
} from '../lib/format'
import {
  canAuthorizeReception,
  canRegisterReception,
} from '../lib/permissions'

interface FormState {
  id_proveedor: number
  id_insumo: number
  peso_bruto_kg: string
  tara_kg: string
  peso_sacos_kg: string
  humedad_pct: string
  autorizar: boolean
}

const statusFilters: Array<{ value: 'TODAS' | EstadoRecepcion; label: string }> = [
  { value: 'TODAS', label: 'Todas' },
  { value: 'PENDIENTE', label: 'Pendientes' },
  { value: 'ACEPTADA', label: 'Aceptadas' },
  { value: 'RECHAZADA', label: 'Rechazadas' },
]

export function RecepcionPage() {
  const { user } = useAuth()
  const { notify, notifyError } = useToast()
  const [recepciones, setRecepciones] = useState<Recepcion[]>([])
  const [proveedores, setProveedores] = useState<Proveedor[]>([])
  const [insumos, setInsumos] = useState<Insumo[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState<'TODAS' | EstadoRecepcion>('TODAS')
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [authorizingId, setAuthorizingId] = useState<number | null>(null)
  const [form, setForm] = useState<FormState>({
    id_proveedor: 0,
    id_insumo: 0,
    peso_bruto_kg: '',
    tara_kg: '30',
    peso_sacos_kg: '30',
    humedad_pct: '13.5',
    autorizar: false,
  })

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [receptionRows, providerRows, inputRows] = await Promise.all([
        api.getRecepciones(),
        api.getProveedores(),
        api.getInsumos(),
      ])
      setRecepciones(receptionRows)
      setProveedores(providerRows)
      setInsumos(inputRows)
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : 'No se pudieron cargar las recepciones.',
      )
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const filtered = useMemo(() => {
    const term = query.trim().toLowerCase()
    return recepciones.filter((row) => {
      const matchesStatus = status === 'TODAS' || row.estado === status
      const matchesTerm =
        !term ||
        row.proveedor_nombre.toLowerCase().includes(term) ||
        row.insumo_nombre.toLowerCase().includes(term) ||
        String(row.id_recepcion).includes(term)
      return matchesStatus && matchesTerm
    })
  }, [recepciones, query, status])

  if (!user) return null

  const canRegister = canRegisterReception(user.rol)
  const canAuthorize = canAuthorizeReception(user.rol)
  const bruto = Number(form.peso_bruto_kg) || 0
  const tara = Number(form.tara_kg) || 0
  const sacos = Number(form.peso_sacos_kg) || 0
  const hasBruto = bruto > 0
  const neto = hasBruto ? bruto - tara - sacos : 0
  const humedad = Number(form.humedad_pct) || 0
  const band = humidityBand(humedad)
  const pendingCount = recepciones.filter((row) => row.estado === 'PENDIENTE').length
  const acceptedToday = recepciones.filter((row) => {
    const date = new Date(row.fecha_hora)
    const today = new Date()
    return (
      row.estado === 'ACEPTADA' &&
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    )
  }).length

  const openModal = () => {
    setForm({
      id_proveedor: proveedores[0]?.id_proveedor ?? 0,
      id_insumo: insumos[0]?.id_insumo ?? 0,
      peso_bruto_kg: '',
      tara_kg: '30',
      peso_sacos_kg: '30',
      humedad_pct: '13.5',
      autorizar: false,
    })
    setFormError('')
    setModalOpen(true)
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setFormError('')
    if (!form.id_proveedor || !form.id_insumo) {
      setFormError('Seleccione proveedor e insumo.')
      return
    }
    if (bruto <= 0) {
      setFormError('El peso bruto debe ser mayor a cero.')
      return
    }
    if (neto <= 0) {
      setFormError('El peso neto debe ser mayor a cero. Revise tara y sacos.')
      return
    }
    if (humedad <= 0 || humedad > 100) {
      setFormError('Ingrese una humedad válida.')
      return
    }
    setSaving(true)
    try {
      const created = await api.crearRecepcion({
        id_proveedor: form.id_proveedor,
        id_insumo: form.id_insumo,
        peso_bruto_kg: bruto,
        tara_kg: tara,
        peso_sacos_kg: sacos,
        humedad_pct: humedad,
        autorizar: form.autorizar,
      })
      setRecepciones((current) => [created, ...current])
      setModalOpen(false)
      if (created.estado === 'RECHAZADA') {
        notify(
          'La recepción quedó registrada como rechazada por humedad mayor a 15%.',
          'error',
        )
      } else if (created.estado === 'PENDIENTE') {
        notify(
          'Recepción registrada. Requiere autorización del jefe de planta.',
          'info',
        )
      } else {
        notify('Recepción aceptada y stock de maíz actualizado.')
      }
    } catch (saveError) {
      const message =
        saveError instanceof Error
          ? saveError.message
          : 'No se pudo registrar la recepción.'
      setFormError(message)
      notifyError(saveError)
    } finally {
      setSaving(false)
    }
  }

  const handleAuthorize = async (reception: Recepcion) => {
    const confirmed = window.confirm(
      `¿Autorizar la recepción #${reception.id_recepcion} con ${formatNumber(reception.humedad_pct)}% de humedad?`,
    )
    if (!confirmed) return
    setAuthorizingId(reception.id_recepcion)
    try {
      const updated = await api.autorizarRecepcion(reception.id_recepcion)
      setRecepciones((current) =>
        current.map((row) =>
          row.id_recepcion === updated.id_recepcion ? updated : row,
        ),
      )
      notify('Recepción autorizada. El stock de maíz fue actualizado.')
    } catch (authorizeError) {
      notifyError(authorizeError)
    } finally {
      setAuthorizingId(null)
    }
  }

  return (
    <div className="stack-lg">
      <PageHeader
        eyebrow="PO-01 · Recepción y acondicionamiento"
        title="Recepción de maíz"
        description="Registre el peso, la humedad y la autorización de cada lote que ingresa a la planta."
        actions={
          canRegister && (
            <button
              type="button"
              className="button button-primary"
              onClick={openModal}
              disabled={!proveedores.length || !insumos.length}
            >
              <Plus size={18} />
              Nueva recepción
            </button>
          )
        }
      />

      <section className="mini-stats">
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-gold"><Truck size={18} /></span>
          <div>
            <strong>{recepciones.length}</strong>
            <span>Recepciones registradas</span>
          </div>
        </div>
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-blue"><ShieldCheck size={18} /></span>
          <div>
            <strong>{pendingCount}</strong>
            <span>Por autorizar</span>
          </div>
        </div>
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-green"><CheckCircle2 size={18} /></span>
          <div>
            <strong>{acceptedToday}</strong>
            <span>Aceptadas hoy</span>
          </div>
        </div>
      </section>

      {error && (
        <div className="inline-alert inline-alert-error">
          <AlertTriangle size={18} />
          {error}
        </div>
      )}

      <section className="panel">
        <header className="panel-head panel-head-wrap">
          <div>
            <h2>Historial de recepciones</h2>
            <p>Control de humedad y trazabilidad por proveedor.</p>
          </div>
          <div className="table-tools">
            <div className="search-box">
              <Search size={17} aria-hidden="true" />
              <input
                type="search"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Buscar proveedor o insumo"
                aria-label="Buscar recepciones"
              />
            </div>
            <div className="filter-pills" role="group" aria-label="Filtrar por estado">
              {statusFilters.map((filter) => (
                <button
                  key={filter.value}
                  type="button"
                  className={`filter-pill ${status === filter.value ? 'filter-pill-active' : ''}`}
                  onClick={() => setStatus(filter.value)}
                >
                  {filter.label}
                </button>
              ))}
            </div>
          </div>
        </header>

        {loading ? (
          <LoadingBlock label="Cargando recepciones…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={Truck}
            title="Sin recepciones para mostrar"
            description="Registre la primera recepción de maíz o ajuste los filtros de búsqueda."
            action={
              canRegister && (
                <button type="button" className="button button-primary" onClick={openModal}>
                  <Plus size={17} />
                  Registrar recepción
                </button>
              )
            }
          />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Recepción</th>
                  <th>Proveedor</th>
                  <th>Insumo</th>
                  <th className="align-right">Peso neto</th>
                  <th>Humedad</th>
                  <th>Estado</th>
                  <th>Autorización</th>
                  <th className="align-right">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((reception) => {
                  const receptionBand = humidityBand(reception.humedad_pct)
                  return (
                    <tr key={reception.id_recepcion}>
                      <td>
                        <div className="cell-main">
                          <strong>#{String(reception.id_recepcion).padStart(3, '0')}</strong>
                          <span>{formatDateTime(reception.fecha_hora)}</span>
                        </div>
                      </td>
                      <td>
                        <div className="cell-main">
                          <strong>{reception.proveedor_nombre}</strong>
                          <span>Bruto {formatKg(reception.peso_bruto_kg)}</span>
                        </div>
                      </td>
                      <td>{reception.insumo_nombre}</td>
                      <td className="align-right numeric">
                        <strong>{formatNumber(reception.peso_neto_kg)} kg</strong>
                        <span className="cell-sub">
                          Tara {formatKg(reception.tara_kg)} + sacos {formatKg(reception.peso_sacos_kg)}
                        </span>
                      </td>
                      <td>
                        <Badge
                          tone={
                            receptionBand === 'ok'
                              ? 'success'
                              : receptionBand === 'review'
                                ? 'warning'
                                : 'danger'
                          }
                        >
                          <Droplets size={13} />
                          {formatNumber(reception.humedad_pct)}%
                        </Badge>
                      </td>
                      <td><RecepcionBadge estado={reception.estado} /></td>
                      <td className="muted">
                        {reception.autoriza_nombre ?? '—'}
                      </td>
                      <td className="align-right">
                        {reception.estado === 'PENDIENTE' && canAuthorize ? (
                          <button
                            type="button"
                            className="button button-small button-secondary"
                            onClick={() => void handleAuthorize(reception)}
                            disabled={authorizingId === reception.id_recepcion}
                          >
                            {authorizingId === reception.id_recepcion ? (
                              <InlineSpinner />
                            ) : (
                              <ShieldCheck size={15} />
                            )}
                            Autorizar
                          </button>
                        ) : (
                          <span className="muted">—</span>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Modal
        open={modalOpen}
        title="Registrar recepción"
        subtitle="El peso neto se calcula como peso bruto menos tara y sacos."
        onClose={() => setModalOpen(false)}
        size="lg"
        footer={
          <>
            <button
              type="button"
              className="button button-secondary"
              onClick={() => setModalOpen(false)}
              disabled={saving}
            >
              Cancelar
            </button>
            <button
              type="submit"
              form="recepcion-form"
              className="button button-primary"
              disabled={saving}
            >
              {saving ? <InlineSpinner /> : <CheckCircle2 size={17} />}
              {saving ? 'Registrando…' : 'Registrar recepción'}
            </button>
          </>
        }
      >
        <form id="recepcion-form" className="form-grid" onSubmit={handleSubmit}>
          <label className="field">
            <span>Proveedor</span>
            <select
              value={form.id_proveedor}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  id_proveedor: Number(event.target.value),
                }))
              }
            >
              {proveedores.map((provider) => (
                <option key={provider.id_proveedor} value={provider.id_proveedor}>
                  {provider.razon_social}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Insumo</span>
            <select
              value={form.id_insumo}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  id_insumo: Number(event.target.value),
                }))
              }
            >
              {insumos.map((input) => (
                <option key={input.id_insumo} value={input.id_insumo}>
                  {input.nombre}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Peso bruto (kg)</span>
            <input
              type="number"
              min="0"
              step="0.01"
              value={form.peso_bruto_kg}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  peso_bruto_kg: event.target.value,
                }))
              }
              placeholder="Ej. 2060"
              required
            />
          </label>

          <label className="field">
            <span>Humedad (%)</span>
            <input
              type="number"
              min="0"
              max="100"
              step="0.1"
              value={form.humedad_pct}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  humedad_pct: event.target.value,
                  autorizar: false,
                }))
              }
              required
            />
          </label>

          <label className="field">
            <span>Tara del vehículo (kg)</span>
            <input
              type="number"
              min="0"
              step="0.01"
              value={form.tara_kg}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  tara_kg: event.target.value,
                }))
              }
            />
          </label>

          <label className="field">
            <span>Peso de sacos (kg)</span>
            <input
              type="number"
              min="0"
              step="0.01"
              value={form.peso_sacos_kg}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  peso_sacos_kg: event.target.value,
                }))
              }
            />
          </label>

          <div className="form-span-2 weight-summary">
            <div>
              <Scale size={18} />
              <span>Peso neto calculado</span>
            </div>
            <strong
              className={
                !hasBruto ? 'muted' : neto > 0 ? 'text-success' : 'text-danger'
              }
            >
              {hasBruto ? `${formatNumber(neto)} kg` : '—'}
            </strong>
          </div>

          <div className={`form-span-2 humidity-hint humidity-${band}`}>
            {band === 'ok' && (
              <>
                <CheckCircle2 size={18} />
                <div>
                  <strong>Humedad aceptable</strong>
                  <span>El lote puede aceptarse directamente (≤ 14.5%).</span>
                </div>
              </>
            )}
            {band === 'review' && (
              <>
                <AlertTriangle size={18} />
                <div>
                  <strong>Requiere autorización del jefe de planta</strong>
                  <span>Humedad entre 14.5% y 15%. El lote quedará pendiente si no se autoriza.</span>
                </div>
              </>
            )}
            {band === 'reject' && (
              <>
                <XCircle size={18} />
                <div>
                  <strong>Lote rechazado por humedad</strong>
                  <span>Más de 15%. Se registrará como rechazado y no ingresará stock.</span>
                </div>
              </>
            )}
          </div>

          {band === 'review' && canAuthorize && (
            <label className="checkbox-field form-span-2">
              <input
                type="checkbox"
                checked={form.autorizar}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    autorizar: event.target.checked,
                  }))
                }
              />
              <span>
                Autorizo el ingreso de este lote pese a la humedad registrada.
              </span>
            </label>
          )}

          {band === 'review' && !canAuthorize && (
            <p className="form-note form-span-2">
              Su rol no puede autorizar esta recepción. Se guardará como pendiente
              para revisión del jefe de planta.
            </p>
          )}

          {formError && (
            <p className="form-error form-span-2" role="alert">
              {formError}
            </p>
          )}
        </form>
      </Modal>
    </div>
  )
}
