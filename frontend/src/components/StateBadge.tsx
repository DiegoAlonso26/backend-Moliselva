import type {
  EstadoDespacho,
  EstadoLote,
  EstadoPedido,
  EstadoRecepcion,
} from '../api/types'
import { Badge, type BadgeTone } from './Badge'

const receptionMap: Record<EstadoRecepcion, { label: string; tone: BadgeTone }> = {
  PENDIENTE: { label: 'Pendiente de autorización', tone: 'warning' },
  ACEPTADA: { label: 'Aceptada', tone: 'success' },
  RECHAZADA: { label: 'Rechazada', tone: 'danger' },
}

const loteMap: Record<EstadoLote, { label: string; tone: BadgeTone }> = {
  PLANIFICADO: { label: 'Planificado', tone: 'info' },
  EN_PROCESO: { label: 'En proceso', tone: 'warning' },
  TERMINADO: { label: 'Terminado', tone: 'success' },
  ANULADO: { label: 'Anulado', tone: 'neutral' },
}

const pedidoMap: Record<EstadoPedido, { label: string; tone: BadgeTone }> = {
  PENDIENTE: { label: 'Pendiente', tone: 'warning' },
  EN_PREPARACION: { label: 'En preparación', tone: 'info' },
  ATENDIDO: { label: 'Atendido', tone: 'success' },
  CANCELADO: { label: 'Cancelado', tone: 'danger' },
}

const despachoMap: Record<EstadoDespacho, { label: string; tone: BadgeTone }> = {
  BORRADOR: { label: 'Borrador', tone: 'warning' },
  CONFIRMADO: { label: 'Confirmado', tone: 'success' },
  ANULADO: { label: 'Anulado', tone: 'neutral' },
}

export function RecepcionBadge({ estado }: { estado: EstadoRecepcion }) {
  const value = receptionMap[estado]
  return (
    <Badge tone={value.tone} dot>
      {value.label}
    </Badge>
  )
}

export function LoteBadge({ estado }: { estado: EstadoLote }) {
  const value = loteMap[estado]
  return (
    <Badge tone={value.tone} dot>
      {value.label}
    </Badge>
  )
}

export function PedidoBadge({ estado }: { estado: EstadoPedido }) {
  const value = pedidoMap[estado]
  return (
    <Badge tone={value.tone} dot>
      {value.label}
    </Badge>
  )
}

export function DespachoBadge({ estado }: { estado: EstadoDespacho }) {
  const value = despachoMap[estado]
  return (
    <Badge tone={value.tone} dot>
      {value.label}
    </Badge>
  )
}
