import type {
  DecisionInput,
  Movimiento,
  Po01ApiClient,
  Po01Session,
  Proveedor,
  Recepcion,
  RecepcionInput,
  StockMaiz,
} from './types'

import { request } from '../shared/http'

export const realPo01Api: Po01ApiClient = {
  login(usuario: string, password: string): Promise<Po01Session> {
    return request<Po01Session>(
      '/auth/login',
      { method: 'POST', body: JSON.stringify({ usuario, password }) },
      false,
    )
  },

  getProveedores(): Promise<Proveedor[]> {
    return request<Proveedor[]>('/proveedores')
  },

  getRecepciones(fecha?: string): Promise<Recepcion[]> {
    const query = fecha ? `?fecha=${encodeURIComponent(fecha)}` : ''
    return request<Recepcion[]>(`/recepciones${query}`)
  },

  crearRecepcion(input: RecepcionInput): Promise<Recepcion> {
    return request<Recepcion>('/recepciones', {
      method: 'POST',
      body: JSON.stringify(input),
    })
  },

  decidirRecepcion(idRecepcion: number, input: DecisionInput): Promise<Recepcion> {
    return request<Recepcion>(`/recepciones/${idRecepcion}/decision`, {
      method: 'PATCH',
      body: JSON.stringify(input),
    })
  },

  getKardex(): Promise<Movimiento[]> {
    return request<Movimiento[]>('/inventario/maiz/kardex')
  },

  getStockMaiz(): Promise<StockMaiz> {
    return request<StockMaiz>('/inventario/maiz/stock')
  },
}
