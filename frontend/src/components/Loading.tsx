export function LoadingBlock({ label = 'Cargando información…' }: { label?: string }) {
  return (
    <div className="loading-block" role="status">
      <span className="spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}

export function InlineSpinner() {
  return <span className="spinner spinner-small" aria-hidden="true" />
}
