import type {
  ApiClient,
  DashboardData,
  Despacho,
  DespachoInput,
  FormulaDetalle,
  FormulaResumen,
  Lote,
  LoteInput,
  LoteRealInput,
  Movimiento,
  Pedido,
  PedidoInput,
  Recepcion,
  RecepcionInput,
  Session,
  StockResumen,
} from './types'

const BASE_URL = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '')
const SESSION_KEY = 'moliselva_session'

function authToken(): string | null {
  const raw = window.localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    const session = JSON.parse(raw) as Session
    return session.token ?? null
  } catch {
    return null
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')
  if (options.body) headers.set('Content-Type', 'application/json')
  const token = authToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    let message = `Error ${response.status} al comunicarse con la API.`
    try {
      const body = (await response.json()) as {
        message?: string
        error?: string
        detail?: string
      }
      message = body.message ?? body.error ?? body.detail ?? message
    } catch {
      const text = await response.text().catch(() => '')
      if (text) message = text
    }
    throw new Error(message)
  }

  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

function jsonBody(value: unknown): string {
  return JSON.stringify(value)
}

export const realApi: ApiClient = {
  login(usuario, password): Promise<Session> {
    return request<Session>('/api/auth/login', {
      method: 'POST',
      body: jsonBody({ usuario, password }),
    })
  },

  getDashboard(): Promise<DashboardData> {
    return request<DashboardData>('/api/reportes/dashboard')
  },

  getProveedores() {
    return request<Awaited<ReturnType<ApiClient['getProveedores']>>>(
      '/api/proveedores',
    )
  },

  getClientes() {
    return request<Awaited<ReturnType<ApiClient['getClientes']>>>('/api/clientes')
  },

  getInsumos() {
    return request<Awaited<ReturnType<ApiClient['getInsumos']>>>('/api/insumos')
  },

  getProductos() {
    return request<Awaited<ReturnType<ApiClient['getProductos']>>>('/api/productos')
  },

  getFormulas(): Promise<FormulaResumen[]> {
    return request<FormulaResumen[]>('/api/formulas')
  },

  getFormula(idFormula, tamanoKg): Promise<FormulaDetalle> {
    return request<FormulaDetalle>(
      `/api/formulas/${idFormula}?tamano_kg=${encodeURIComponent(tamanoKg)}`,
    )
  },

  getRecepciones(): Promise<Recepcion[]> {
    return request<Recepcion[]>('/api/recepciones')
  },

  crearRecepcion(input: RecepcionInput): Promise<Recepcion> {
    return request<Recepcion>('/api/recepciones', {
      method: 'POST',
      body: jsonBody(input),
    })
  },

  autorizarRecepcion(idRecepcion): Promise<Recepcion> {
    return request<Recepcion>(`/api/recepciones/${idRecepcion}/autorizar`, {
      method: 'POST',
    })
  },

  getLotes(): Promise<Lote[]> {
    return request<Lote[]>('/api/lotes')
  },

  crearLote(input: LoteInput): Promise<Lote> {
    return request<Lote>('/api/lotes', {
      method: 'POST',
      body: jsonBody(input),
    })
  },

  confirmarNucleo(idLote): Promise<Lote> {
    return request<Lote>(`/api/lotes/${idLote}/nucleo`, { method: 'POST' })
  },

  terminarLote(idLote, input: LoteRealInput): Promise<Lote> {
    return request<Lote>(`/api/lotes/${idLote}/terminar`, {
      method: 'POST',
      body: jsonBody(input),
    })
  },

  getStock(): Promise<StockResumen> {
    return request<StockResumen>('/api/inventario/stock')
  },

  getKardex(): Promise<Movimiento[]> {
    return request<Movimiento[]>('/api/inventario/kardex')
  },

  getPedidos(): Promise<Pedido[]> {
    return request<Pedido[]>('/api/pedidos')
  },

  crearPedido(input: PedidoInput): Promise<Pedido> {
    return request<Pedido>('/api/pedidos', {
      method: 'POST',
      body: jsonBody(input),
    })
  },

  getDespachos(): Promise<Despacho[]> {
    return request<Despacho[]>('/api/despachos')
  },

  crearDespacho(input: DespachoInput): Promise<Despacho> {
    return request<Despacho>('/api/despachos', {
      method: 'POST',
      body: jsonBody(input),
    })
  },

  confirmarDespacho(idDespacho): Promise<Despacho> {
    return request<Despacho>(`/api/despachos/${idDespacho}/confirmar`, {
      method: 'POST',
    })
  },
}
