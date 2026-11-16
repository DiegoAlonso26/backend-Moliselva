import { useCallback, useEffect, useMemo, useState } from 'react'
import { Plus, RefreshCw, Search } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { PageHeader } from '../components/PageHeader'
import { useToast } from '../components/Toast'
import { request, send } from '../shared/http'
import { RecordForm } from './RecordForm'
import { formatDateTime } from '../lib/format'
import type { Action, Resource, Row } from './model'

type Dialog = { type: 'create' | 'edit' | 'delete' | 'action'; row?: Row; action?: Action }
const noFields: Resource['fields'] = []
export function todayLima() {
  const parts = new Intl.DateTimeFormat('en', { timeZone: 'America/Lima', year: 'numeric', month: '2-digit', day: '2-digit' }).formatToParts()
  const part = (key: string) => parts.find(p => p.type === key)?.value
  return `${part('year')}-${part('month')}-${part('day')}`
}
function text(value: unknown) {
  if (value == null || value === '') return '—'
  if (typeof value === 'boolean') return value ? 'Activo' : 'Inactivo'
  return String(value).replaceAll('_', ' ')
}

export function ResourcePage({ config, embedded = false }: { config: Resource; embedded?: boolean }) {
  const { user } = useAuth()
  const { notify } = useToast()
  const [rows, setRows] = useState<Row[]>([])
  const [references, setReferences] = useState<Record<string, Row[]>>({})
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')
  const [date, setDate] = useState(todayLima)
  const [page, setPage] = useState(0)
  const [detail, setDetail] = useState<Row | null>(null)
  const [dialog, setDialog] = useState<Dialog | null>(null)
  const canRead = Boolean(user && config.read.includes(user.rol))
  const canWrite = Boolean(user && config.write?.includes(user.rol))
  const itemPath = (row: Row) => config.itemPath?.(row) ?? `${config.path}/${row[config.id]}`
  const load = useCallback(async () => {
    if (!canRead) return
    setLoading(true)
    setError('')
    try {
      const paths = [...new Set(config.columns.flatMap(c => c.reference ? [c.reference.path] : []))]
      const [data, labels] = await Promise.all([
        request<Row[]>(`${config.path}${config.filterDate ? `?fecha=${encodeURIComponent(date)}` : ''}`),
        Promise.all(paths.map(async path => [path, await request<Row[]>(path)] as const)),
      ])
      setRows(config.map ? data.map(config.map) : data)
      setReferences(Object.fromEntries(labels))
    } catch (e) { setError(e instanceof Error ? e.message : 'No se pudo cargar la información.') }
    finally { setLoading(false) }
  }, [config, date, canRead])
  useEffect(() => { setDetail(null); setPage(0); void load() }, [load])
  function display(row: Row, column: Resource['columns'][number]) {
    if (column.key === 'fechaHora' && row[column.key]) return formatDateTime(String(row[column.key]))
    const reference = column.reference
    if (!reference || row[column.key] == null) return text(row[column.key])
    const found = references[reference.path]?.find(r => String(r[reference.id]) === String(row[column.key]))
    return found ? String(found[reference.label]) : `#${row[column.key]} (referencia histórica)`
  }
  const filtered = useMemo(() => rows.filter(row => (!status || String(row.estado) === status)
    && (/^#\d+$/.test(query) ? String(row[config.id]) === query.slice(1) : config.columns.some(column => display(row, column).toLocaleLowerCase().includes(query.toLocaleLowerCase())))), [rows, query, status, references, config])
  const statuses = [...new Set(rows.flatMap(row => typeof row.estado === 'string' ? [row.estado] : []))]
  const pages = Math.max(1, Math.ceil(filtered.length / 20))
  const currentPage = Math.min(page, pages - 1)
  const editable = (row: Row) => !config.noEdit && (config.editable?.(row) ?? true)
  const deletable = (row: Row) => !config.noDelete && (config.deletable?.(row, user?.id ?? 0) ?? (config.editable?.(row) ?? true))
  async function view(row: Row) {
    setError('')
    try {
      const result = await request<Row>(itemPath(row))
      setDetail(config.map ? config.map(result) : result)
    } catch (e) { setError(e instanceof Error ? e.message : 'No se pudo consultar el registro.') }
  }
  async function mutate(values: Row) {
    if (!dialog) return
    const { type, row, action } = dialog
    if (type === 'create' || type === 'edit') {
      const invalid = config.validate?.(values)
      if (invalid) throw new Error(invalid)
      const body = config.payload ? config.payload(values, type === 'edit') : values
      const result = await send<Row>(type === 'edit' ? itemPath(row!) : config.createPath || config.path, type === 'edit' ? 'PUT' : 'POST', body)
      if (type === 'create' && result?.[config.id] != null) {
        setQuery(`#${result[config.id]}`)
        setStatus('')
        setPage(0)
        if (config.filterDate) setDate(todayLima())
      }
      if (result && config.children) setDetail(result)
      else if (detail && type === 'edit') setDetail(result)
      notify(type === 'edit' ? 'Cambios guardados.' : 'Registro creado.')
    } else if (type === 'delete') {
      await send(itemPath(row!), 'DELETE')
      if (detail?.[config.id] === row![config.id]) setDetail(null)
      notify(config.deactivate ? 'Registro desactivado. El historial se conserva.' : 'Registro eliminado.')
    } else if (action) {
      const result = await send<Row>(`${itemPath(row!)}${action.suffix}`, action.method, { ...action.body, ...values })
      if (result && detail?.[config.id] === row![config.id]) setDetail(config.map ? config.map(result) : result)
      notify('Operación completada.')
    }
    // El resultado de la escritura ya está confirmado; un fallo al refrescar se muestra aparte.
    void load()
  }
  if (!canRead) return <div className="panel" role="alert">Su rol no tiene acceso a esta sección.</div>
  const children = detail && canWrite ? config.children?.(detail).filter(c => user && c.read.includes(user.rol)) : []
  return <section className={embedded ? 'resource-section resource-embedded' : 'resource-section'}>
    {embedded ? <div className="resource-subheading"><h2>{config.title}</h2><p>{config.description}</p></div>
      : <PageHeader eyebrow="Gestión de planta" title={config.title} description={config.description} />}
    {config.warning && <div className="notice">{config.warning}</div>}
    <div className="panel resource-panel">
      <div className="resource-toolbar">
        <label className="resource-search"><Search size={17} /><span className="sr-only">Buscar en {config.title.toLowerCase()}</span><input aria-label={`Buscar en ${config.title.toLowerCase()}`} placeholder="Buscar…" value={query} onChange={e => { setQuery(e.target.value); setPage(0) }} /></label>
        {config.filterDate && <label>Fecha<input aria-label="Fecha de recepción" type="date" required value={date} onChange={e => { if (e.target.value) setDate(e.target.value) }} /></label>}
        {statuses.length > 0 && <select aria-label="Filtrar por estado" value={status} onChange={e => { setStatus(e.target.value); setPage(0) }}><option value="">Todos los estados</option>{statuses.map(s => <option key={s} value={s}>{text(s)}</option>)}</select>}
        <button className="button button-secondary" onClick={() => void load()} disabled={loading}><RefreshCw size={16} />Actualizar</button>
        {canWrite && !config.noCreate && <button className="button button-primary" onClick={() => setDialog({ type: 'create' })}><Plus size={16} />{config.createLabel || `Crear ${config.singular}`}</button>}
      </div>
      {error && <div role="alert" className="form-error">{error}<button className="button button-secondary" onClick={() => void load()}>Reintentar</button></div>}
      {loading && <p className="resource-message" role="status">Cargando registros…</p>}
      {!loading && !error && !filtered.length && <div className="resource-message"><h3>No hay registros para mostrar</h3><p>{query || status ? 'Pruebe con otros filtros.' : 'Los registros aparecerán aquí cuando se guarden en el sistema.'}</p></div>}
      {!loading && filtered.length > 0 && <div className="resource-table-scroll"><table className="resource-table"><thead><tr>{config.columns.map(c => <th key={c.key}>{c.label}</th>)}<th>Acciones</th></tr></thead><tbody>{filtered.slice(currentPage * 20, currentPage * 20 + 20).map(row => <tr key={itemPath(row)}>{config.columns.map(c => <td key={c.key}>{c.key === 'estado' || c.key === 'activo' ? <span className="resource-status">{display(row, c)}</span> : display(row, c)}</td>)}<td><div className="row-actions">
        <button className="button button-secondary button-sm" onClick={() => void view(row)}>Ver detalle</button>
        {canWrite && editable(row) && <button className="button button-secondary button-sm" onClick={() => setDialog({ type: 'edit', row })}>Editar</button>}
        {canWrite && deletable(row) && <button className="button button-secondary button-sm danger-text" onClick={() => setDialog({ type: 'delete', row })}>{config.deactivate ? 'Desactivar' : 'Eliminar'}</button>}
        {canWrite && config.actions?.filter(a => (!a.roles || a.roles.includes(user!.rol)) && (!a.available || a.available(row))).map(action => <button key={action.label} className="button button-secondary button-sm" onClick={() => setDialog({ type: 'action', row, action })}>{action.label}</button>)}
      </div></td></tr>)}</tbody></table></div>}
      <div className="resource-pagination"><small>{filtered.length} registro(s){config.limit ? ` · Consulta limitada a los últimos ${config.limit}` : ''}</small><button className="button button-secondary button-sm" disabled={currentPage === 0} onClick={() => setPage(currentPage - 1)}>Anterior</button><span>{currentPage + 1} / {pages}</span><button className="button button-secondary button-sm" disabled={currentPage + 1 >= pages} onClick={() => setPage(currentPage + 1)}>Siguiente</button></div>
    </div>
    {detail && <section className="panel resource-detail" aria-label={`Detalle de ${config.singular}`}><div className="resource-detail-heading"><h2>Detalle de {config.singular} #{detail[config.id]}</h2><button className="button button-secondary" onClick={() => setDetail(null)}>Cerrar detalle</button></div><dl className="record-summary">{config.columns.map(column => <div key={column.key}><dt>{column.label}</dt><dd>{display(detail, column)}</dd></div>)}</dl>
      {children?.map(child => <ResourcePage config={child} embedded key={child.path} />)}
    </section>}
    {dialog && <RecordForm title={dialog.type === 'action' ? dialog.action!.label : `${dialog.type === 'create' ? 'Crear' : dialog.type === 'edit' ? 'Editar' : config.deactivate ? 'Desactivar' : 'Eliminar'} ${config.singular}`}
      description={dialog.type === 'delete' ? `Confirme la operación sobre el registro #${dialog.row![config.id]}. ${config.deactivate ? 'Se conservarán sus referencias históricas.' : 'El servidor verificará que el registro permita esta operación.'}` : dialog.action?.description || (config.children ? 'Guarde el encabezado y continúe con los detalles. Cada operación queda guardada por separado.' : undefined)}
      fields={dialog.type === 'delete' ? noFields : dialog.type === 'action' ? dialog.action?.fields || noFields : config.fields}
      initial={dialog.row} editing={dialog.type === 'edit'} onSave={mutate} onClose={() => setDialog(null)}
      preview={dialog.type === 'create' || dialog.type === 'edit' ? config.preview : undefined}
      submitLabel={dialog.type === 'delete' ? 'Confirmar' : dialog.type === 'action' ? dialog.action!.label : 'Guardar'} />}
  </section>
}
