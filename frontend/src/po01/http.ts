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

const API_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? '/api'
const SESSION_KEY = 'moliselva_session'

function token(): string | null {
  const raw = window.sessionStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    return (JSON.parse(raw) as Po01Session).token ?? null
  } catch {
    return null
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  withToken = true,
): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')
  if (options.body) headers.set('Content-Type', 'application/json')
  if (withToken) {
    const current = token()
    if (current) headers.set('Authorization', `Bearer ${current}`)
  }
  const response = await fetch(`${API_URL}${path}`, { ...options, headers })
  const contentType = response.headers.get('Content-Type') ?? ''
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text()
  if (!response.ok) {
    const message =
      typeof payload === 'object' && payload && 'mensaje' in payload
        ? String((payload as { mensaje: unknown }).mensaje)
        : `Error ${response.status} al comunicarse con la API.`
    throw new Error(message)
  }
  return payload as T
}

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
