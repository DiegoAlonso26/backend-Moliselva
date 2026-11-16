import { useEffect, useId, useState, type FormEvent } from 'react'
import { Modal } from '../components/Modal'
import { request } from '../shared/http'
import type { Field, Row } from './model'

export function RecordForm({ title, description, fields, initial, editing = false, submitLabel = 'Guardar', onSave, onClose, preview }: {
  title: string; description?: string; fields: Field[]; initial?: Row; editing?: boolean
  submitLabel?: string; onSave: (values: Row) => Promise<void>; onClose: () => void
  preview?: (values: Row) => string
}) {
  const id = useId()
  const [values, setValues] = useState<Record<string, string>>(() => Object.fromEntries(fields.map(f =>
    [f.key, f.type === 'password' ? '' : String(initial?.[f.key] ?? f.default ?? '')])))
  const [references, setReferences] = useState<Record<string, Row[]>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    let active = true
    setLoading(true)
    const paths = [...new Set(fields.flatMap(f => f.reference ? [f.reference.path] : []))]
    Promise.all(paths.map(async p => [p, await request<Row[]>(p)] as const))
      .then(data => { if (active) { setReferences(Object.fromEntries(data)); setError('') } })
      .catch(e => { if (active) setError(e instanceof Error ? e.message : 'No se pudieron cargar las opciones.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [fields, retry])

  async function save(event: FormEvent) {
    event.preventDefault()
    setError('')
    const data: Row = {}
    for (const field of fields) {
      const value = field.type === 'password' ? values[field.key] ?? '' : values[field.key]?.trim() ?? ''
      if (editing && field.type === 'password' && !value) continue
      if (field.required && !value && !(editing && field.type === 'password')) { setError(`Complete ${field.label.toLowerCase()}.`); return }
      data[field.key] = value === '' ? null : field.type === 'number' || field.reference ? Number(value) : value
    }
    setBusy(true)
    try { await onSave(data); onClose() }
    catch (e) { setError(e instanceof Error ? e.message : 'No se pudo guardar. Revise los datos.') }
    finally { setBusy(false) }
  }
  return <Modal open title={title} subtitle={description} onClose={() => { if (!busy) onClose() }}>
    <form onSubmit={save} className="record-form">
      {loading && <p role="status">Cargando opciones…</p>}
      {error && <div className="form-error" role="alert">{error}{loading === false && fields.some(f => f.reference && !references[f.reference.path]) && <button type="button" onClick={() => setRetry(n => n + 1)}>Reintentar carga</button>}</div>}
      <div className="record-fields">{fields.map(field => {
        const reference = field.reference
        const rows = reference ? (references[reference.path] || []).filter(r => !reference.filter || reference.filter(r, values)) : []
        const options = reference ? rows.map(r => ({ value: String(r[reference.id]), label: `${r[reference.label] ?? `Registro #${r[reference.id]}`} · #${r[reference.id]}` }))
          : (field.options || []).map(value => ({ value, label: value === 'true' ? 'Sí' : value === 'false' ? 'No' : value.replaceAll('_', ' ') }))
        const previous = reference && values[field.key] && !options.some(o => o.value === values[field.key])
        const controlId = `${id}-${field.key}`
        const props = { id: controlId, name: field.key, value: values[field.key], disabled: busy || loading || (editing && field.immutable),
          required: field.required && !(editing && field.type === 'password'),
          onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => setValues(v => {
            const next = { ...v, [field.key]: e.target.value }
            for (const dependent of fields) {
              const ref = dependent.reference
              if (dependent.key !== field.key && ref?.filter && !(references[ref.path] || []).some(r => String(r[ref.id]) === next[dependent.key] && ref.filter!(r, next))) next[dependent.key] = ''
            }
            return next
          }) }
        return <div className="record-field" key={field.key}>
          <label htmlFor={controlId}>{field.label}{props.required && <span aria-hidden="true"> *</span>}</label>
          {reference || field.type === 'select' ? <select {...props}><option value="">Seleccione…</option>{previous && <option value={values[field.key]}>Registro #{values[field.key]} (referencia anterior)</option>}{options.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}</select>
            : field.type === 'textarea' ? <textarea {...props} rows={3} maxLength={field.maxLength} />
              : <input {...props} type={field.type || 'text'} min={field.min} max={field.max} step={field.step} maxLength={field.maxLength} minLength={field.type === 'password' ? 12 : undefined} autoComplete={field.type === 'password' ? 'new-password' : 'off'} />}
          {field.hint && <small>{field.hint}</small>}
          {reference && !loading && !rows.length && <small>No hay opciones disponibles. Revise el catálogo relacionado.</small>}
          {editing && field.type === 'password' && <small>Déjela vacía para conservar la contraseña actual.</small>}
        </div>
      })}</div>
      {preview && <p className="notice" role="status">{preview(values)}</p>}
      <div className="record-form-actions"><button className="button button-secondary" type="button" onClick={onClose} disabled={busy}>Cancelar</button><button className="button button-primary" type="submit" disabled={busy || loading}>{busy ? 'Guardando…' : submitLabel}</button></div>
    </form>
  </Modal>
}
