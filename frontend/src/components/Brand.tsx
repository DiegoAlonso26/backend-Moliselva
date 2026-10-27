export function BrandMark({ light = false }: { light?: boolean }) {
  return (
    <span className={`brand-mark ${light ? 'brand-mark-light' : ''}`} aria-hidden="true">
      <span className="brand-mark-letter">M</span>
      <span className="brand-mark-rule" />
    </span>
  )
}

export function Brand({
  compact = false,
  light = false,
}: {
  compact?: boolean
  light?: boolean
}) {
  return (
    <span className={`brand ${compact ? 'brand-compact' : ''} ${light ? 'brand-light' : ''}`}>
      <BrandMark light={light} />
      <span className="brand-copy">
        <strong>Moliselva</strong>
        <small>Planta Tarapoto</small>
      </span>
    </span>
  )
}
