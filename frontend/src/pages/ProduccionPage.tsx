import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  AlertTriangle,
  CheckCircle2,
  ClipboardList,
  Factory,
  FlaskConical,
  LockKeyhole,
  Plus,
  Search,
  Scale,
} from 'lucide-react'
import { api } from '../api'
import type {
  EstadoLote,
  FormulaDetalle,
  FormulaResumen,
  Lote,
  Producto,
} from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { Badge } from '../components/Badge'
import { EmptyState } from '../components/EmptyState'
import { InlineSpinner, LoadingBlock } from '../components/Loading'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { LoteBadge } from '../components/StateBadge'
import { useToast } from '../components/Toast'
import {
  formatDateTime,
  formatKg,
  formatNumber,
  percent,
} from '../lib/format'
import {
  canConfirmNucleo,
  canCreateLote,
  canFinishLote,
  canSeeFormula,
} from '../lib/permissions'

const statusFilters: Array<{ value: 'TODOS' | EstadoLote; label: string }> = [
  { value: 'TODOS', label: 'Todos' },
  { value: 'PLANIFICADO', label: 'Planificados' },
  { value: 'EN_PROCESO', label: 'En proceso' },
  { value: 'TERMINADO', label: 'Terminados' },
]

export function ProduccionPage() {
  const { user } = useAuth()
  const { notify, notifyError } = useToast()
  const [lotes, setLotes] = useState<Lote[]>([])
  const [productos, setProductos] = useState<Producto[]>([])
  const [formulas, setFormulas] = useState<FormulaResumen[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState<'TODOS' | EstadoLote>('TODOS')
  const [newOpen, setNewOpen] = useState(false)
  const [newSaving, setNewSaving] = useState(false)
  const [newError, setNewError] = useState('')
  const [selectedFormulaId, setSelectedFormulaId] = useState(0)
  const [tamano, setTamano] = useState('500')
  const [preview, setPreview] = useState<FormulaDetalle | null>(null)
  const [previewLoading, setPreviewLoading] = useState(false)
  const [previewError, setPreviewError] = useState('')
  const [closingLote, setClosingLote] = useState<Lote | null>(null)
  const [realValues, setRealValues] = useState<Record<number, string>>({})
  const [closing, setClosing] = useState(false)
  const [closingError, setClosingError] = useState('')
  const [detailLote, setDetailLote] = useState<Lote | null>(null)
  const [actionId, setActionId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [loteRows, productRows, formulaRows] = await Promise.all([
        api.getLotes(),
        api.getProductos(),
        api.getFormulas(),
      ])
      setLotes(loteRows)
      setProductos(productRows)
      setFormulas(formulaRows)
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : 'No se pudieron cargar los lotes.',
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
    return lotes.filter((row) => {
      const matchesStatus = status === 'TODOS' || row.estado === status
      const matchesTerm =
        !term ||
        row.producto_nombre.toLowerCase().includes(term) ||
        row.responsable_nombre.toLowerCase().includes(term) ||
        String(row.id_lote).includes(term)
      return matchesStatus && matchesTerm
    })
  }, [lotes, query, status])

  if (!user) return null

  const canCreate = canCreateLote(user.rol)
  const canConfirm = canConfirmNucleo(user.rol)
  const canFinish = canFinishLote(user.rol)
  const showComposition = canSeeFormula(user.rol)
  const selectedFormula = formulas.find((row) => row.id_formula === selectedFormulaId)
  const selectedProduct = productos.find(
    (row) => row.id_producto === selectedFormula?.id_producto,
  )
  const scale = (Number(tamano) || 0) / 1000
  const previewMacros =
    preview?.macros.map((macro) => ({
      ...macro,
      cantidad_calculada_kg: Number(
        (macro.cantidad_calculada_kg * scale).toFixed(3),
      ),
    })) ?? []
  const previewTotal = previewMacros.reduce(
    (total, macro) => total + macro.cantidad_calculada_kg,
    0,
  )

  const loadPreview = async (idFormula: number) => {
    setPreviewLoading(true)
    setPreviewError('')
    try {
      const detail = await api.getFormula(idFormula, 1000)
      setPreview(detail)
    } catch (loadError) {
      setPreview(null)
      setPreviewError(
        loadError instanceof Error
          ? loadError.message
          : 'No se pudo cargar la fórmula.',
      )
    } finally {
      setPreviewLoading(false)
    }
  }

  const openNew = () => {
    const first = formulas[0]
    setSelectedFormulaId(first?.id_formula ?? 0)
    setTamano('500')
    setNewError('')
    setPreview(null)
    setNewOpen(true)
    if (first) void loadPreview(first.id_formula)
  }

  const handleFormulaChange = (idFormula: number) => {
    setSelectedFormulaId(idFormula)
    void loadPreview(idFormula)
  }

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault()
    setNewError('')
    const size = Number(tamano)
    if (!selectedFormula) {
      setNewError('Seleccione una fórmula.')
      return
    }
    if (!Number.isFinite(size) || size <= 0) {
      setNewError('El tamaño del lote debe ser mayor a cero.')
      return
    }
    setNewSaving(true)
    try {
      const created = await api.crearLote({
        id_producto: selectedFormula.id_producto,
        id_formula: selectedFormula.id_formula,
        tamano_kg: size,
      })
      setLotes((current) => [created, ...current])
      setNewOpen(false)
      notify('Lote planificado. El núcleo debe ser confirmado por el jefe de planta.')
    } catch (saveError) {
      const message =
        saveError instanceof Error ? saveError.message : 'No se pudo crear el lote.'
      setNewError(message)
      notifyError(saveError)
    } finally {
      setNewSaving(false)
    }
  }

  const handleConfirmNucleo = async (lote: Lote) => {
    const confirmed = window.confirm(
      `¿Confirmar el núcleo del lote #${lote.id_lote} y pasar a producción?`,
    )
    if (!confirmed) return
    setActionId(lote.id_lote)
    try {
      const updated = await api.confirmarNucleo(lote.id_lote)
      setLotes((current) =>
        current.map((row) => (row.id_lote === updated.id_lote ? updated : row)),
      )
      notify('Núcleo confirmado. El lote está en proceso.')
    } catch (actionError) {
      notifyError(actionError)
    } finally {
      setActionId(null)
    }
  }

  const openClose = (lote: Lote) => {
    setClosingLote(lote)
    setClosingError('')
    setRealValues(
      Object.fromEntries(
        lote.macros.map((macro) => [
          macro.id_insumo,
          String(macro.cantidad_calculada_kg),
        ]),
      ),
    )
  }

  const handleCloseLote = async (event: FormEvent) => {
    event.preventDefault()
    if (!closingLote) return
    setClosingError('')
    const macros = closingLote.macros.map((macro) => ({
      id_insumo: macro.id_insumo,
      cantidad_real_kg: Number(realValues[macro.id_insumo]) || 0,
    }))
    if (macros.some((macro) => macro.cantidad_real_kg <= 0)) {
      setClosingError('Registre un consumo real mayor a cero para cada insumo.')
      return
    }
    setClosing(true)
    try {
      const updated = await api.terminarLote(closingLote.id_lote, { macros })
      setLotes((current) =>
        current.map((row) => (row.id_lote === updated.id_lote ? updated : row)),
      )
      setClosingLote(null)
      notify(
        'Lote terminado. Los insumos se descontaron y el producto ingresó al inventario.',
      )
    } catch (closeError) {
      const message =
        closeError instanceof Error
          ? closeError.message
          : 'No se pudo terminar el lote.'
      setClosingError(message)
      notifyError(closeError)
    } finally {
      setClosing(false)
    }
  }

  const realTotal = closingLote
    ? closingLote.macros.reduce(
        (total, macro) =>
          total + (Number(realValues[macro.id_insumo]) || 0),
        0,
      )
    : 0
  const plannedCount = lotes.filter((row) => row.estado === 'PLANIFICADO').length
  const processCount = lotes.filter((row) => row.estado === 'EN_PROCESO').length
  const doneCount = lotes.filter((row) => row.estado === 'TERMINADO').length
  const doneKg = lotes
    .filter((row) => row.estado === 'TERMINADO')
    .reduce((total, row) => total + row.tamano_kg, 0)

  return (
    <div className="stack-lg">
      <PageHeader
        eyebrow="PO-03 · Producción por lote"
        title="Producción por lote"
        description="Planifique la producción, confirme el núcleo y registre el consumo real de insumos."
        actions={
          canCreate && (
            <button
              type="button"
              className="button button-primary"
              onClick={openNew}
              disabled={!formulas.length}
            >
              <Plus size={18} />
              Nuevo lote
            </button>
          )
        }
      />

      <section className="mini-stats">
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-blue"><ClipboardList size={18} /></span>
          <div>
            <strong>{plannedCount}</strong>
            <span>Planificados</span>
          </div>
        </div>
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-gold"><Factory size={18} /></span>
          <div>
            <strong>{processCount}</strong>
            <span>En proceso</span>
          </div>
        </div>
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-green"><CheckCircle2 size={18} /></span>
          <div>
            <strong>{doneCount}</strong>
            <span>Terminados</span>
          </div>
        </div>
        <div className="mini-stat">
          <span className="mini-stat-icon mini-stat-green"><Scale size={18} /></span>
          <div>
            <strong>{formatNumber(doneKg)} kg</strong>
            <span>Producidos</span>
          </div>
        </div>
      </section>

      {!showComposition && (
        <div className="inline-alert inline-alert-info">
          <LockKeyhole size={18} />
          Su rol puede operar lotes, pero la composición porcentual de las fórmulas
          está reservada. Solo se muestran las cantidades necesarias.
        </div>
      )}

      {error && (
        <div className="inline-alert inline-alert-error">
          <AlertTriangle size={18} />
          {error}
        </div>
      )}

      <section className="panel">
        <header className="panel-head panel-head-wrap">
          <div>
            <h2>Lotes de producción</h2>
            <p>Estado, responsable y consumo de cada lote.</p>
          </div>
          <div className="table-tools">
            <div className="search-box">
              <Search size={17} aria-hidden="true" />
              <input
                type="search"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Buscar producto o responsable"
                aria-label="Buscar lotes"
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
          <LoadingBlock label="Cargando lotes…" />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={Factory}
            title="Sin lotes para mostrar"
            description="Planifique un lote para iniciar la producción de alimento balanceado."
            action={
              canCreate && (
                <button type="button" className="button button-primary" onClick={openNew}>
                  <Plus size={17} />
                  Planificar lote
                </button>
              )
            }
          />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Lote</th>
                  <th>Producto</th>
                  <th>Fórmula</th>
                  <th className="align-right">Tamaño</th>
                  <th>Estado</th>
                  <th>Responsable</th>
                  <th className="align-right">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((lote) => (
                  <tr key={lote.id_lote}>
                    <td>
                      <div className="cell-main">
                        <strong>Lote #{String(lote.id_lote).padStart(3, '0')}</strong>
                        <span>{formatDateTime(lote.fecha_hora)}</span>
                      </div>
                    </td>
                    <td><strong>{lote.producto_nombre}</strong></td>
                    <td>
                      <Badge tone="neutral">{lote.formula_version}</Badge>
                    </td>
                    <td className="align-right numeric">{formatKg(lote.tamano_kg)}</td>
                    <td><LoteBadge estado={lote.estado} /></td>
                    <td className="muted">{lote.responsable_nombre}</td>
                    <td className="align-right">
                      <div className="row-actions">
                        {lote.estado === 'PLANIFICADO' && canConfirm && (
                          <button
                            type="button"
                            className="button button-small button-secondary"
                            onClick={() => void handleConfirmNucleo(lote)}
                            disabled={actionId === lote.id_lote}
                          >
                            {actionId === lote.id_lote ? (
                              <InlineSpinner />
                            ) : (
                              <CheckCircle2 size={15} />
                            )}
                            Confirmar núcleo
                          </button>
                        )}
                        {lote.estado === 'PLANIFICADO' && !canConfirm && (
                          <span className="muted small-text">Jefe debe confirmar</span>
                        )}
                        {lote.estado === 'EN_PROCESO' && canFinish && (
                          <button
                            type="button"
                            className="button button-small button-primary"
                            onClick={() => openClose(lote)}
                          >
                            Terminar lote
                          </button>
                        )}
                        {lote.estado === 'TERMINADO' && (
                          <button
                            type="button"
                            className="button button-small button-ghost"
                            onClick={() => setDetailLote(lote)}
                          >
                            Ver detalle
                          </button>
                        )}
                        {lote.estado === 'EN_PROCESO' && !canFinish && (
                          <button
                            type="button"
                            className="button button-small button-ghost"
                            onClick={() => setDetailLote(lote)}
                          >
                            Ver detalle
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Modal
        open={newOpen}
        title="Planificar nuevo lote"
        subtitle="La fórmula se calcula para el tamaño indicado. El stock se descuenta al terminar."
        onClose={() => setNewOpen(false)}
        size="lg"
        footer={
          <>
            <button
              type="button"
              className="button button-secondary"
              onClick={() => setNewOpen(false)}
              disabled={newSaving}
            >
              Cancelar
            </button>
            <button
              type="submit"
              form="nuevo-lote-form"
              className="button button-primary"
              disabled={newSaving || previewLoading}
            >
              {newSaving ? <InlineSpinner /> : <Plus size={17} />}
              {newSaving ? 'Planificando…' : 'Planificar lote'}
            </button>
          </>
        }
      >
        <form id="nuevo-lote-form" className="form-grid" onSubmit={handleCreate}>
          <label className="field form-span-2">
            <span>Producto y fórmula vigente</span>
            <select
              value={selectedFormulaId}
              onChange={(event) => handleFormulaChange(Number(event.target.value))}
            >
              {formulas.map((formula) => (
                <option key={formula.id_formula} value={formula.id_formula}>
                  {formula.producto_nombre} · {formula.version}
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Tamaño del lote (kg)</span>
            <input
              type="number"
              min="1"
              step="1"
              value={tamano}
              onChange={(event) => setTamano(event.target.value)}
              required
            />
          </label>

          <div className="field">
            <span>Producto</span>
            <div className="readonly-value">
              {selectedProduct?.nombre ?? 'Sin producto'}
            </div>
          </div>

          <div className="form-span-2">
            <div className="formula-preview-head">
              <div>
                <strong>Vista previa de fórmula</strong>
                <span>
                  Calculada para {formatKg(Number(tamano) || 0)} de producto.
                </span>
              </div>
              {showComposition ? (
                <Badge tone="brand">Composición visible</Badge>
              ) : (
                <Badge tone="neutral">
                  <LockKeyhole size={12} />
                  Composición reservada
                </Badge>
              )}
            </div>

            {previewLoading ? (
              <div className="formula-preview-loading">
                <InlineSpinner /> Calculando fórmula…
              </div>
            ) : previewError ? (
              <p className="form-error">{previewError}</p>
            ) : previewMacros.length === 0 ? (
              <p className="muted">Seleccione una fórmula para ver la vista previa.</p>
            ) : (
              <div className="formula-preview">
                {previewMacros.map((macro) => (
                  <div key={macro.id_insumo} className="formula-row">
                    <div>
                      <strong>{macro.insumo}</strong>
                      {showComposition && (
                        <span>{percent(macro.proporcion_pct)} de la mezcla</span>
                      )}
                    </div>
                    <span className="numeric">{formatKg(macro.cantidad_calculada_kg)}</span>
                  </div>
                ))}
                <div className="formula-total">
                  <span>Total de insumos</span>
                  <strong>{formatKg(previewTotal)}</strong>
                </div>
              </div>
            )}
          </div>

          {preview && !showComposition && <p className="form-note form-span-2">{preview.nota}</p>}

          <div className="form-span-2 inline-alert inline-alert-info">
            <FlaskConical size={18} />
            El lote quedará planificado. El jefe de planta debe confirmar el núcleo
            antes de registrar el consumo real.
          </div>

          {newError && (
            <p className="form-error form-span-2" role="alert">
              {newError}
            </p>
          )}
        </form>
      </Modal>

      <Modal
        open={Boolean(closingLote)}
        title={`Terminar lote #${String(closingLote?.id_lote ?? '').padStart(3, '0')}`}
        subtitle={closingLote?.producto_nombre}
        onClose={() => setClosingLote(null)}
        size="lg"
        footer={
          <>
            <button
              type="button"
              className="button button-secondary"
              onClick={() => setClosingLote(null)}
              disabled={closing}
            >
              Cancelar
            </button>
            <button
              type="submit"
              form="terminar-lote-form"
              className="button button-primary"
              disabled={closing}
            >
              {closing ? <InlineSpinner /> : <CheckCircle2 size={17} />}
              {closing ? 'Terminando…' : 'Terminar y actualizar stock'}
            </button>
          </>
        }
      >
        {closingLote && (
          <form id="terminar-lote-form" className="form-grid" onSubmit={handleCloseLote}>
            <div className="form-span-2 close-summary">
              <div>
                <span>Tamaño planificado</span>
                <strong>{formatKg(closingLote.tamano_kg)}</strong>
              </div>
              <div>
                <span>Consumo real registrado</span>
                <strong>{formatKg(realTotal)}</strong>
              </div>
              <div>
                <span>Diferencia</span>
                <strong className={realTotal > closingLote.tamano_kg ? 'text-danger' : ''}>
                  {formatKg(realTotal - closingLote.tamano_kg)}
                </strong>
              </div>
            </div>

            <div className="form-span-2 consumption-table">
              <div className="consumption-head">
                <span>Insumo</span>
                <span className="align-right">Calculado</span>
                <span className="align-right">Consumo real (kg)</span>
              </div>
              {closingLote.macros.map((macro) => (
                <div key={macro.id_insumo} className="consumption-row">
                  <div>
                    <strong>{macro.insumo}</strong>
                    {macro.proporcion_pct > 0 && (
                      <span>{percent(macro.proporcion_pct)} de la mezcla</span>
                    )}
                  </div>
                  <span className="numeric align-right">
                    {formatNumber(macro.cantidad_calculada_kg)}
                  </span>
                  <input
                    type="number"
                    min="0"
                    step="0.001"
                    value={realValues[macro.id_insumo] ?? ''}
                    onChange={(event) =>
                      setRealValues((current) => ({
                        ...current,
                        [macro.id_insumo]: event.target.value,
                      }))
                    }
                    aria-label={`Consumo real de ${macro.insumo}`}
                  />
                </div>
              ))}
            </div>

            <p className="form-note form-span-2">
              El producto terminado ingresará al inventario por{' '}
              <strong>{formatKg(closingLote.tamano_kg)}</strong>. Los insumos se
              descontarán por las cantidades reales registradas.
            </p>

            {closingError && (
              <p className="form-error form-span-2" role="alert">
                {closingError}
              </p>
            )}
          </form>
        )}
      </Modal>

      <Modal
        open={Boolean(detailLote)}
        title={`Detalle del lote #${String(detailLote?.id_lote ?? '').padStart(3, '0')}`}
        subtitle={detailLote ? `${detailLote.producto_nombre} · ${detailLote.formula_version}` : undefined}
        onClose={() => setDetailLote(null)}
        size="lg"
        footer={
          <button
            type="button"
            className="button button-secondary"
            onClick={() => setDetailLote(null)}
          >
            Cerrar
          </button>
        }
      >
        {detailLote && (
          <div className="stack-md">
            <div className="detail-grid">
              <div>
                <span>Estado</span>
                <LoteBadge estado={detailLote.estado} />
              </div>
              <div>
                <span>Tamaño</span>
                <strong>{formatKg(detailLote.tamano_kg)}</strong>
              </div>
              <div>
                <span>Responsable</span>
                <strong>{detailLote.responsable_nombre}</strong>
              </div>
              <div>
                <span>Fecha</span>
                <strong>{formatDateTime(detailLote.fecha_hora)}</strong>
              </div>
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Insumo</th>
                    {showComposition && <th className="align-right">%</th>}
                    <th className="align-right">Calculado</th>
                    <th className="align-right">Real</th>
                  </tr>
                </thead>
                <tbody>
                  {detailLote.macros.map((macro) => (
                    <tr key={macro.id_insumo}>
                      <td>{macro.insumo}</td>
                      {showComposition && (
                        <td className="align-right numeric">
                          {macro.proporcion_pct > 0 ? percent(macro.proporcion_pct) : '—'}
                        </td>
                      )}
                      <td className="align-right numeric">
                        {formatKg(macro.cantidad_calculada_kg)}
                      </td>
                      <td className="align-right numeric">
                        {macro.cantidad_real_kg !== null
                          ? formatKg(macro.cantidad_real_kg)
                          : '—'}
                      </td>
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
