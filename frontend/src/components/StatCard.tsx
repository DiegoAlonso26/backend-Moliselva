import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'

export function StatCard({
  icon: Icon,
  label,
  value,
  helper,
  tone = 'green',
  footer,
}: {
  icon: LucideIcon
  label: string
  value: ReactNode
  helper?: string
  tone?: 'green' | 'gold' | 'blue' | 'red'
  footer?: ReactNode
}) {
  return (
    <article className={`stat-card stat-${tone}`}>
      <div className="stat-icon">
        <Icon size={22} aria-hidden="true" />
      </div>
      <div className="stat-content">
        <p className="stat-label">{label}</p>
        <p className="stat-value">{value}</p>
        {helper && <p className="stat-helper">{helper}</p>}
        {footer}
      </div>
    </article>
  )
}
