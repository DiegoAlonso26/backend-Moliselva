const dateTimeFormatter = new Intl.DateTimeFormat('es-PE', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
})

const dateFormatter = new Intl.DateTimeFormat('es-PE', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
})

const longDateFormatter = new Intl.DateTimeFormat('es-PE', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
})

const numberFormatter = new Intl.NumberFormat('es-PE', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
})

export function formatDateTime(isoDate: string): string {
  return dateTimeFormatter.format(new Date(isoDate))
}

export function formatDate(isoDate: string): string {
  return dateFormatter.format(new Date(isoDate))
}

export function formatLongDate(date = new Date()): string {
  const value = longDateFormatter.format(date)
  return value.charAt(0).toUpperCase() + value.slice(1)
}

export function formatNumber(value: number, digits = 2): string {
  if (!Number.isFinite(value)) return '0'
  return new Intl.NumberFormat('es-PE', {
    minimumFractionDigits: 0,
    maximumFractionDigits: digits,
  }).format(value)
}

export function formatKg(value: number): string {
  return `${formatNumber(value)} kg`
}

export function formatSacos(value: number): string {
  return `${numberFormatter.format(value)} ${value === 1 ? 'saco' : 'sacos'}`
}

const userAliases: Record<string, string> = {
  admin_demo: 'Lucía Mendoza',
  jefe_demo: 'María Quispe',
  operario_demo: 'Carlos Ríos',
  contadora_demo: 'Ana Torres',
}

export function displayUser(value: string | null | undefined): string {
  if (!value) return '—'
  return userAliases[value] ?? value
}

export function humidityBand(value: number): 'ok' | 'review' | 'reject' {
  if (value > 15) return 'reject'
  if (value > 14.5) return 'review'
  return 'ok'
}

export function percent(value: number): string {
  return `${formatNumber(value)}%`
}

export function downloadCsv(
  filename: string,
  rows: Array<Array<string | number>>,
): void {
  const content = rows
    .map((row) =>
      row
        .map((cell) => `"${String(cell).replaceAll('"', '""')}"`)
        .join(','),
    )
    .join('\n')
  const blob = new Blob([`\uFEFF${content}`], {
    type: 'text/csv;charset=utf-8;',
  })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(url)
}
