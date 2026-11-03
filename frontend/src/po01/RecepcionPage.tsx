import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  Droplets,
  PackageCheck,
  Plus,
  ShieldCheck,
  Truck,
  XCircle,
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { Badge } from '../components/Badge'
import { EmptyState } from '../components/EmptyState'
import { InlineSpinner, LoadingBlock } from '../components/Loading'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { useToast } from '../components/Toast'
import {
  displayUser,
  formatDateTime,
  formatKg,
  formatNumber,
  humidityBand,
} from '../lib/format'
import { po01Api } from './api'
import {
  canDecideReception,
  canRegisterReception,
} from './permissions'
import type {
  DecisionRecepcion,
  EstadoRecepcion,
  Proveedor,
  Recepcion,
} from './types'

interface FormState {
  idProveedor: number
  pesoBrutoKg: string
  taraKg: string
  pesoSacosKg: string
  humedadPct: string
  mermaLimpiezaKg: string
  llegoEnSacos: boolean
}

const initialForm: FormState = {
  idProveedor: 0,
  pesoBrutoKg: '',
  taraKg: '0',
  pesoSacosKg: '0',
  humedadPct: '',
  mermaLimpiezaKg: '0',
  llegoEnSacos: false,
}

function fechaHoyLocal(): string {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function estadoBadge(estado: EstadoRecepcion) {
  if (estado === 'ACEPTADA') {
    return <Badge tone="success" dot>Aceptada</Badge>
  }
  if (estado === 'PENDIENTE') {
    return <Badge tone="warning" dot>Pendiente</Badge>
  }
  return <Badge tone="danger" dot>Rechazada</Badge>
}

export function RecepcionPage() {
  const { user } = useAuth()
  const { notify, notifyError } = useToast()
  const [recepciones, setRecepciones] = useState<Recepcion[]>([])
  const [proveedores, setProveedores] = useState<Proveedor[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [form, setForm] = useState<FormState>(initialForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)
  const [resultado, setResultado] = useState<Recepcion | null>(null)
  const [selected, setSelected] = useState<Recepcion | null>(null)
  const [decision, setDecision] = useState<DecisionRecepcion>('ACEPTAR')
  const [motivo, setMotivo] = useState('')
  const [decisionError, setDecisionError] = useState('')
  const [deciding, setDeciding] = useState(false)
  const [filtroEstado, setFiltroEstado] = useState<'TODAS' | EstadoRecepcion>('TODAS')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [receptionRows, providerRows] = await Promise.all([
        po01Api.getRecepciones(fechaHoyLocal()),
        po01Api.getProveedores(),
      ])
      setRecepciones(receptionRows)
      setProveedores(providerRows)
      setForm((current) => ({
        ...current,
        idProveedor: current.idProveedor || providerRows[0]?.idProveedor || 0,
      }))
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

  const bruto = Number(form.pesoBrutoKg) || 0
  const tara = Number(form.taraKg) || 0
  const sacos = form.llegoEnSacos ? Number(form.pesoSacosKg) || 0 : 0
  const merma = Number(form.mermaLimpiezaKg) || 0
  const humedad = Number(form.humedadPct)
  const tieneHumedad = form.humedadPct.trim() !== '' && Number.isFinite(humedad)
  const neto = bruto > 0 ? bruto - tara - sacos : 0
  const entrada = neto > 0 ? Math.max(0, neto - merma) : 0
  const band = humidityBand(tieneHumedad ? humedad : 0)
  const canRegister = user ? canRegisterReception(user.rol) : false
  const canDecide = user ? canDecideReception(user.rol) : false

  const pendientes = recepciones.filter((row) => row.estado === 'PENDIENTE').length
  const showRegistradoPor = recepciones.some((row) => Boolean(row.registradoPor))
  const aceptadasHoy = recepciones.filter((row) => row.estado === 'ACEPTADA')
  const kgAceptados = useMemo(
    () => aceptadasHoy.reduce((sum, row) => sum + (row.entradaKardexKg ?? 0), 0),
    [aceptadasHoy],
  )
  const recepcionesFiltradas = useMemo(
    () =>
      filtroEstado === 'TODAS'
        ? recepciones
        : recepciones.filter((row) => row.estado === filtroEstado),
    [filtroEstado, recepciones],
  )
  const humidityPosition = tieneHumedad
    ? Math.min(100, Math.max(0, ((humedad - 12) / 6) * 100))
    : 0

  const update = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm((current) => ({ ...current, [key]: value }))
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setFormError('')
    setResultado(null)
    if (!form.idProveedor) {
      setFormError('Seleccione un proveedor.')
      return
    }
    if (bruto <= 0) {
      setFormError('El peso bruto debe ser mayor a cero.')
      return
    }
    if (tara < 0 || sacos < 0 || merma < 0) {
      setFormError('Los pesos y la merma no pueden ser negativos.')
      return
    }
    if (tara >= bruto) {
      setFormError('La tara debe ser menor que el peso bruto.')
      return
    }
    if (!Number.isFinite(humedad) || humedad < 0 || humedad > 100) {
      setFormError('Ingrese una humedad entre 0 y 100.')
      return
    }
    if (neto <= 0) {
      setFormError('El peso neto debe ser mayor a cero. Revise tara y sacos.')
      return
    }
    if (merma >= neto) {
      setFormError('La merma de limpieza debe ser menor que el peso neto.')
      return
    }
    setSaving(true)
    try {
      const created = await po01Api.crearRecepcion({
        idProveedor: form.idProveedor,
        pesoBrutoKg: bruto,
        taraKg: tara,
        pesoSacosKg: sacos,
        humedadPct: humedad,
        mermaLimpiezaKg: merma,
      })
      setRecepciones((current) => [
        created,
        ...current.filter((row) => row.idRecepcion !== created.idRecepcion),
      ])
      setResultado(created)
      setForm((current) => ({
        ...initialForm,
        idProveedor: current.idProveedor,
      }))
      if (created.estado === 'ACEPTADA') {
        notify('Recepción aceptada. La entrada ya está en el kardex de maíz.')
      } else if (created.estado === 'PENDIENTE') {
        notify('Recepción registrada. Requiere decisión del jefe de planta.', 'info')
      } else {
        notify('Recepción rechazada por humedad mayor a 15%.', 'error')
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

  const openDecision = (reception: Recepcion) => {
    setSelected(reception)
    setDecision('ACEPTAR')
    setMotivo('')
    setDecisionError('')
  }

  const handleDecision = async (event: FormEvent) => {
    event.preventDefault()
    if (!selected) return
    setDecisionError('')
    if (!motivo.trim()) {
      setDecisionError('Escriba el motivo de la decisión.')
      return
    }
    setDeciding(true)
    try {
      const updated = await po01Api.decidirRecepcion(selected.idRecepcion, {
        decision,
        motivo: motivo.trim(),
      })
      setRecepciones((current) =>
        current.map((row) =>
          row.idRecepcion === updated.idRecepcion ? updated : row,
        ),
      )
      setResultado(updated)
      setSelected(null)
      notify(
        updated.estado === 'ACEPTADA'
          ? 'Recepción aceptada y registrada en el kardex de maíz.'
          : 'Recepción rechazada. No se modificó el kardex.',
        updated.estado === 'ACEPTADA' ? 'success' : 'info',
      )
    } catch (decisionFailure) {
      const message =
        decisionFailure instanceof Error
          ? decisionFailure.message
          : 'No se pudo registrar la decisión.'
      setDecisionError(message)
    } finally {
      setDeciding(false)
    }
  }

  return (
    <div className="stack-lg">
      <PageHeader
        eyebrow="Operación de planta · Maíz"
        title="Recepción de maíz"
        description="Registre el pesaje y el control de calidad de cada carga. La entrada al kardex se genera solo cuando la recepción queda aceptada."
        actions={
          <div className="po01-page-chip">
            <ShieldCheck size={16} aria-hidden="true" />
            {canRegister ? 'Registro habilitado' : 'Vista de solo lectura'}
          </div>
        }
      />

      <div className="stats-grid stats-grid-3">
        <StatCard
          icon={Truck}
          label="Recepciones de hoy"
          value={recepciones.length}
          helper="Registros del turno"
        />
        <StatCard
          icon={Clock3}
          label="Pendientes de decisión"
          value={pendientes}
          helper="Requieren autorización"
          tone="gold"
        />
        <StatCard
          icon={PackageCheck}
          label="Ingreso al kardex"
          value={formatKg(kgAceptados)}
          helper="Cargas aceptadas hoy"
          tone="blue"
        />
      </div>

      <div className="po01-grid">
        <section className="panel">
          <div className="panel-head">
            <div>
              <h2>Registro de carga</h2>
              <p>Pesaje en balanza y análisis de calidad de la carga recibida.</p>
            </div>
            {canRegister && (
              <Badge tone="brand">
                <Plus size={13} aria-hidden="true" /> Registro
              </Badge>
            )}
          </div>

          {canRegister ? (
            <form
              className="form-grid"
              onSubmit={handleSubmit}
              data-testid="recepcion-form"
            >
              <div className="form-section-label form-span-2">
                <span>Procedencia y pesaje</span>
                <small>Guía de transporte y lectura de balanza</small>
              </div>

              <label className="field form-span-2">
                <span>Proveedor</span>
                <select
                  value={form.idProveedor}
                  onChange={(event) => update('idProveedor', Number(event.target.value))}
                  data-testid="select-proveedor"
                >
                  <option value={0}>Seleccione un proveedor</option>
                  {proveedores.map((provider) => (
                    <option key={provider.idProveedor} value={provider.idProveedor}>
                      {provider.razonSocial}
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
                  inputMode="decimal"
                  value={form.pesoBrutoKg}
                  onChange={(event) => update('pesoBrutoKg', event.target.value)}
                  placeholder="32500"
                  data-testid="input-bruto"
                />
              </label>

              <label className="field">
                <span>Tara (kg)</span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  inputMode="decimal"
                  value={form.taraKg}
                  onChange={(event) => update('taraKg', event.target.value)}
                  placeholder="12500"
                  data-testid="input-tara"
                />
              </label>

              <label className="checkbox-field form-span-2">
                <input
                  type="checkbox"
                  checked={form.llegoEnSacos}
                  onChange={(event) => {
                    update('llegoEnSacos', event.target.checked)
                    if (!event.target.checked) update('pesoSacosKg', '0')
                  }}
                  data-testid="input-sacos-check"
                />
                <span>
                  <strong>La carga llegó en sacos</strong>
                  <br />
                  Descuente el peso de los envases del peso bruto.
                </span>
              </label>

              <label className="field">
                <span>Peso de los sacos (kg)</span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  inputMode="decimal"
                  value={form.pesoSacosKg}
                  onChange={(event) => update('pesoSacosKg', event.target.value)}
                  disabled={!form.llegoEnSacos}
                  data-testid="input-sacos"
                />
              </label>

              <div className="form-section-label form-span-2">
                <span>Control de calidad</span>
                <small>Lectura del medidor de humedad y limpieza</small>
              </div>

              <label className="field">
                <span>Humedad (%)</span>
                <input
                  type="number"
                  min="0"
                  max="100"
                  step="0.1"
                  inputMode="decimal"
                  value={form.humedadPct}
                  onChange={(event) => update('humedadPct', event.target.value)}
                  placeholder="14.2"
                  data-testid="input-humedad"
                />
              </label>

              <label className="field">
                <span>Merma de limpieza (kg)</span>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  inputMode="decimal"
                  value={form.mermaLimpiezaKg}
                  onChange={(event) => update('mermaLimpiezaKg', event.target.value)}
                  data-testid="input-merma"
                />
              </label>

              <div className="reception-summary form-span-2">
                <div>
                  <span>Peso neto</span>
                  <strong data-testid="peso-neto">{formatKg(Math.max(0, neto))}</strong>
                </div>
                <div>
                  <span>Merma aplicada</span>
                  <strong>{formatKg(merma)}</strong>
                </div>
                <div className="reception-summary-entry">
                  <span>Entrada al kardex</span>
                  <strong>{formatKg(entrada)}</strong>
                </div>
              </div>

              <div
                className={`humidity-hint form-span-2 ${
                  !tieneHumedad
                    ? 'humidity-neutral'
                    : band === 'ok'
                      ? 'humidity-ok'
                      : band === 'review'
                        ? 'humidity-review'
                        : 'humidity-reject'
                }`}
              >
                {!tieneHumedad ? (
                  <Droplets size={17} aria-hidden="true" />
                ) : band === 'ok' ? (
                  <CheckCircle2 size={17} aria-hidden="true" />
                ) : band === 'review' ? (
                  <AlertTriangle size={17} aria-hidden="true" />
                ) : (
                  <XCircle size={17} aria-hidden="true" />
                )}
                <div>
                  <strong>
                    {!tieneHumedad
                      ? 'Ingrese la humedad'
                      : band === 'ok'
                        ? 'Calidad conforme'
                        : band === 'review'
                          ? 'Requiere autorización del jefe de planta'
                          : 'Carga rechazada'}
                  </strong>
                  <span>
                    {!tieneHumedad
                      ? 'La decisión se calcula con el porcentaje medido.'
                      : band === 'ok'
                        ? 'Hasta 14.5%: la entrada se genera automáticamente.'
                        : band === 'review'
                          ? 'Más de 14.5% y hasta 15%: queda pendiente de decisión.'
                          : 'Más de 15%: no entra al kardex.'}
                  </span>
                </div>
              </div>

              <div className="form-span-2 po01-form-actions">
                <span className="po01-entry-note">
                  El registro queda asociado a su usuario y a la hora de ingreso.
                </span>
                <button
                  type="submit"
                  className="button button-primary"
                  disabled={saving}
                  data-testid="btn-registrar"
                >
                  {saving ? <InlineSpinner /> : <Plus size={17} />}
                  {saving ? 'Registrando…' : 'Registrar recepción'}
                </button>
              </div>

              {formError && (
                <p className="form-error form-span-2" role="alert">
                  {formError}
                </p>
              )}
            </form>
          ) : (
            <div className="inline-alert inline-alert-info">
              <ShieldCheck size={18} aria-hidden="true" />
              <div>
                Su rol puede consultar las recepciones del día, pero no registrar
                ni decidir. Esas acciones corresponden al operario y al jefe de
                planta.
              </div>
            </div>
          )}

          {resultado && (
            <div
              className={`inline-alert ${
                resultado.estado === 'ACEPTADA'
                  ? 'inline-alert-info'
                  : resultado.estado === 'PENDIENTE'
                    ? 'inline-alert-warning'
                    : 'inline-alert-error'
              }`}
              data-testid="resultado-recepcion"
            >
              {resultado.estado === 'ACEPTADA' ? (
                <CheckCircle2 size={18} aria-hidden="true" />
              ) : resultado.estado === 'PENDIENTE' ? (
                <AlertTriangle size={18} aria-hidden="true" />
              ) : (
                <XCircle size={18} aria-hidden="true" />
              )}
              <div>
                <strong>Recepción #{resultado.idRecepcion}: {resultado.estado.toLowerCase()}</strong>
                <span>
                  {resultado.estado === 'ACEPTADA'
                    ? `Ingresaron ${formatKg(resultado.entradaKardexKg ?? 0)} al kardex de maíz.`
                    : resultado.estado === 'PENDIENTE'
                      ? 'El jefe de planta debe aceptar o rechazar con un motivo.'
                      : 'No se modificó el kardex de maíz.'}
                </span>
              </div>
            </div>
          )}
        </section>

        <aside className="stack-md">
          <section className="panel po01-rules">
            <div className="panel-head">
              <div>
                <h2>Control de calidad</h2>
                <p>El estado se calcula con la humedad registrada.</p>
              </div>
              <Droplets size={19} aria-hidden="true" />
            </div>

            <div className="quality-summary">
              <div className="quality-reading">
                <span>Humedad medida</span>
                <strong>{tieneHumedad ? `${formatNumber(humedad)}%` : '—'}</strong>
                <small>
                  {!tieneHumedad
                    ? 'Pendiente de lectura'
                    : band === 'ok'
                      ? 'Dentro del rango de recepción'
                      : band === 'review'
                        ? 'Fuera del rango automático'
                        : 'Por encima del límite'}
                </small>
              </div>
              <div className="quality-scale" aria-hidden="true">
                <div className="quality-scale-track">
                  <span className="quality-scale-ok" />
                  <span className="quality-scale-review" />
                  <span className="quality-scale-reject" />
                  {tieneHumedad && (
                    <span
                      className="quality-scale-marker"
                      style={{ left: `${humidityPosition}%` }}
                    />
                  )}
                </div>
                <div className="quality-scale-labels">
                  <span>12%</span>
                  <span>14.5%</span>
                  <span>15%</span>
                  <span>18%</span>
                </div>
              </div>
            </div>

            <div className="po01-rule-list">
              <div className="po01-rule po01-rule-ok">
                <span className="po01-rule-dot" />
                <div>
                  <strong>Hasta 14.5%</strong>
                  <span>Se acepta y entra al kardex.</span>
                </div>
              </div>
              <div className="po01-rule po01-rule-review">
                <span className="po01-rule-dot" />
                <div>
                  <strong>Más de 14.5% y hasta 15%</strong>
                  <span>Queda pendiente de decisión del jefe.</span>
                </div>
              </div>
              <div className="po01-rule po01-rule-reject">
                <span className="po01-rule-dot" />
                <div>
                  <strong>Más de 15%</strong>
                  <span>Se rechaza y no entra al kardex.</span>
                </div>
              </div>
            </div>
            <p className="form-note">
              La recepción aceptada genera una sola entrada. Las correcciones se
              registran como ajuste; el kardex no se edita ni se borra.
            </p>
          </section>

          <section className="panel po01-trace">
            <div className="panel-head">
              <div>
                <h2>Responsabilidades</h2>
                <p>Quién registra y quién autoriza.</p>
              </div>
              <ShieldCheck size={19} aria-hidden="true" />
            </div>
            <ul className="po01-check-list">
              <li>Operario y jefe de planta registran la recepción.</li>
              <li>Solo el jefe acepta o rechaza una carga pendiente.</li>
              <li>El kardex conserva usuario, fecha y origen.</li>
              <li>El operario no puede exportar ni autorizar.</li>
            </ul>
          </section>
        </aside>
      </div>

      <section className="panel">
        <div className="panel-head panel-head-wrap">
          <div>
            <h2>Recepciones del día</h2>
            <p>Hora, proveedor, peso neto, humedad, estado y responsable.</p>
          </div>
          <Badge tone="neutral">{recepciones.length} registros</Badge>
        </div>

        {recepciones.length > 0 && (
          <div className="table-filters" role="group" aria-label="Filtrar por estado">
            {[
              { value: 'TODAS' as const, label: 'Todas' },
              { value: 'ACEPTADA' as const, label: 'Aceptadas' },
              { value: 'PENDIENTE' as const, label: 'Pendientes' },
              { value: 'RECHAZADA' as const, label: 'Rechazadas' },
            ].map((item) => {
              const count =
                item.value === 'TODAS'
                  ? recepciones.length
                  : recepciones.filter((row) => row.estado === item.value).length
              return (
                <button
                  key={item.value}
                  type="button"
                  className={`table-filter ${filtroEstado === item.value ? 'table-filter-active' : ''}`}
                  onClick={() => setFiltroEstado(item.value)}
                  aria-pressed={filtroEstado === item.value}
                >
                  {item.label}
                  <span>{count}</span>
                </button>
              )
            })}
          </div>
        )}

        {loading ? (
          <LoadingBlock label="Cargando recepciones…" />
        ) : error ? (
          <div className="inline-alert inline-alert-error">
            <AlertTriangle size={18} aria-hidden="true" />
            <div>
              {error}
              <button type="button" className="text-link" onClick={() => void load()}>
                Reintentar
              </button>
            </div>
          </div>
        ) : recepciones.length === 0 ? (
          <EmptyState
            icon={Truck}
            title="Aún no hay recepciones hoy"
            description="Registre la primera recepción para verla aquí y actualizar el kardex de maíz."
          />
        ) : recepcionesFiltradas.length === 0 ? (
          <EmptyState
            icon={Truck}
            title="Sin recepciones en este estado"
            description="Cambie el filtro para consultar el resto de registros del día."
          />
        ) : (
          <>
          <div className="table-wrap">
            <table className="data-table po01-table recepcion-table" data-testid="tabla-recepciones">
              <thead>
                <tr>
                  <th>Hora</th>
                  <th>Proveedor</th>
                  <th>Peso neto</th>
                  <th>Humedad</th>
                  <th>Merma</th>
                  <th>Estado</th>
                  {showRegistradoPor && <th>Registrado por</th>}
                  {canDecide && <th>Decisión</th>}
                </tr>
              </thead>
              <tbody>
                {recepcionesFiltradas.map((row) => (
                  <tr key={row.idRecepcion}>
                    <td>{formatDateTime(row.fechaHora)}</td>
                    <td>
                      <strong>{row.proveedor}</strong>
                      <span className="po01-subtext">#{row.idRecepcion}</span>
                      <span className="po01-mobile-meta">{formatDateTime(row.fechaHora)}</span>
                      <span className="po01-mobile-meta">Neto {formatKg(row.pesoNetoKg)}</span>
                      <span className="po01-mobile-meta">Merma {formatKg(row.mermaLimpiezaKg)}</span>
                    </td>
                    <td>{formatKg(row.pesoNetoKg)}</td>
                    <td>{formatNumber(row.humedadPct)}%</td>
                    <td>{formatKg(row.mermaLimpiezaKg)}</td>
                    <td>{estadoBadge(row.estado)}</td>
                    {showRegistradoPor && <td>{displayUser(row.registradoPor)}</td>}
                    {canDecide && (
                      <td>
                        {row.estado === 'PENDIENTE' ? (
                          <button
                            type="button"
                            className="button button-small button-primary"
                            onClick={() => openDecision(row)}
                            data-testid={`btn-decidir-${row.idRecepcion}`}
                          >
                            Decidir
                          </button>
                        ) : (
                          <span className="po01-subtext">
                            {row.autorizadoPor ? `Por ${displayUser(row.autorizadoPor)}` : '—'}
                          </span>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          </>
        )}
      </section>

      <Modal
        open={Boolean(selected)}
        title={selected ? `Decisión de la recepción #${selected.idRecepcion}` : 'Decisión'}
        subtitle={
          selected
            ? `${selected.proveedor} · ${formatKg(selected.pesoNetoKg)} · ${formatNumber(selected.humedadPct)}% de humedad`
            : undefined
        }
        onClose={() => setSelected(null)}
        footer={
          <>
            <button
              type="button"
              className="button button-secondary"
              onClick={() => setSelected(null)}
            >
              Cancelar
            </button>
            <button
              type="submit"
              form="decision-form"
              className={`button ${
                decision === 'ACEPTAR' ? 'button-primary' : 'button-danger'
              }`}
              disabled={deciding}
              data-testid="btn-confirmar-decision"
            >
              {deciding ? <InlineSpinner /> : null}
              {deciding
                ? 'Guardando…'
                : decision === 'ACEPTAR'
                  ? 'Aceptar recepción'
                  : 'Rechazar recepción'}
            </button>
          </>
        }
      >
        <form id="decision-form" className="stack-md" onSubmit={handleDecision}>
          <div className="inline-alert inline-alert-warning">
            <AlertTriangle size={18} aria-hidden="true" />
            <div>
              La recepción tiene {selected ? formatNumber(selected.humedadPct) : '—'}% de
              humedad. La decisión y el motivo quedan registrados.
            </div>
          </div>

          <div className="po01-decision-options" role="radiogroup" aria-label="Decisión">
            <label className={decision === 'ACEPTAR' ? 'po01-decision-active' : ''}>
              <input
                type="radio"
                name="decision"
                value="ACEPTAR"
                checked={decision === 'ACEPTAR'}
                onChange={() => setDecision('ACEPTAR')}
              />
              <span>
                <strong>Aceptar</strong>
                <small>
                  Ingresan {selected ? formatKg(selected.pesoNetoKg - selected.mermaLimpiezaKg) : '—'} al kardex.
                </small>
              </span>
            </label>
            <label className={decision === 'RECHAZAR' ? 'po01-decision-active' : ''}>
              <input
                type="radio"
                name="decision"
                value="RECHAZAR"
                checked={decision === 'RECHAZAR'}
                onChange={() => setDecision('RECHAZAR')}
              />
              <span>
                <strong>Rechazar</strong>
                <small>No se modifica el kardex de maíz.</small>
              </span>
            </label>
          </div>

          <label className="field">
            <span>Motivo de la decisión</span>
            <textarea
              value={motivo}
              onChange={(event) => setMotivo(event.target.value)}
              placeholder="Ej. Falta de maíz en planta"
              data-testid="decision-motivo"
            />
          </label>

          {decisionError && (
            <p className="form-error" role="alert">
              {decisionError}
            </p>
          )}
        </form>
      </Modal>
    </div>
  )
}
