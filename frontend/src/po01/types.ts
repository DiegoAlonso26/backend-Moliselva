import type { Rol } from '../api/types'

export type { Rol }

export interface Po01Usuario {
  id: number
  nombres: string
  usuario: string
  rol: Rol
}

export interface Po01Session {
  token: string
  usuario: Po01Usuario
}

export interface Proveedor {
  idProveedor: number
  razonSocial: string
}

export type EstadoRecepcion = 'PENDIENTE' | 'ACEPTADA' | 'RECHAZADA'

export interface Recepcion {
  idRecepcion: number
  fechaHora: string
  proveedor: string
  pesoBrutoKg: number
  taraKg: number
  pesoSacosKg: number
  pesoNetoKg: number
  humedadPct: number
  mermaLimpiezaKg: number
  estado: EstadoRecepcion
  entradaKardexKg: number | null
  registradoPor: string
  autorizadoPor: string | null
}

export interface RecepcionInput {
  idProveedor: number
  pesoBrutoKg: number
  taraKg: number
  pesoSacosKg: number
  humedadPct: number
  mermaLimpiezaKg: number
}

export type DecisionRecepcion = 'ACEPTAR' | 'RECHAZAR'

export interface DecisionInput {
  decision: DecisionRecepcion
  motivo: string
}

export type TipoMovimiento = 'ENTRADA' | 'AJUSTE_ENTRADA' | 'AJUSTE_SALIDA'

export interface Movimiento {
  idMovimiento: number
  fechaHora: string
  tipoMovimiento: TipoMovimiento
  cantidadKg: number
  saldoKg: number
  idRecepcion: number | null
  usuario: string
}

export interface StockMaiz {
  stockKg: number
}

export interface Po01ApiClient {
  login(usuario: string, password: string): Promise<Po01Session>
  getProveedores(): Promise<Proveedor[]>
  getRecepciones(fecha?: string): Promise<Recepcion[]>
  crearRecepcion(input: RecepcionInput): Promise<Recepcion>
  decidirRecepcion(idRecepcion: number, input: DecisionInput): Promise<Recepcion>
  getKardex(): Promise<Movimiento[]>
  getStockMaiz(): Promise<StockMaiz>
}
