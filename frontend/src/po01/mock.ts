import type {
  DecisionInput,
  EstadoRecepcion,
  Movimiento,
  Po01ApiClient,
  Po01Session,
  Proveedor,
  Recepcion,
  RecepcionInput,
  StockMaiz,
} from './types'

const STATE_KEY = 'moliselva_po01_state_v1'
const SESSION_KEY = 'moliselva_session'

interface Po01State {
  recepciones: Recepcion[]
  movimientos: Movimiento[]
  nextRecepcion: number
  nextMovimiento: number
}

const usuarios: Array<{
  id: number
  nombres: string
  usuario: string
  password: string
  rol: Po01Session['usuario']['rol']
}> = [
  {
    id: 1,
    nombres: 'Lucía Mendoza',
    usuario: 'admin_demo',
    password: 'demo123',
    rol: 'ADMINISTRADOR',
  },
  {
    id: 2,
    nombres: 'María Quispe',
    usuario: 'jefe_demo',
    password: 'demo123',
    rol: 'JEFE_PLANTA',
  },
  {
    id: 3,
    nombres: 'Carlos Ríos',
    usuario: 'operario_demo',
    password: 'demo123',
    rol: 'OPERARIO',
  },
  {
    id: 4,
    nombres: 'Ana Torres',
    usuario: 'contadora_demo',
    password: 'demo123',
    rol: 'CONTADORA',
  },
]

const proveedores: Proveedor[] = [
  { idProveedor: 1, razonSocial: 'Agropecuaria del Huallaga S.A.C.' },
  { idProveedor: 2, razonSocial: 'Cooperativa Agraria San Martín' },
  { idProveedor: 3, razonSocial: 'Granos del Mayo E.I.R.L.' },
]

function roundKg(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100
}

function todayIso(): string {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function localDate(isoDate: string): string {
  const date = new Date(isoDate)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

function seedState(): Po01State {
  const now = new Date()
  now.setHours(6, 30, 0, 0)
  return {
    recepciones: [],
    movimientos: [
      {
        idMovimiento: 1,
        fechaHora: now.toISOString(),
        tipoMovimiento: 'ENTRADA',
        cantidadKg: 8500,
        saldoKg: 8500,
        idRecepcion: null,
        usuario: 'Sistema',
      },
    ],
    nextRecepcion: 1,
    nextMovimiento: 2,
  }
}

function readState(): Po01State {
  const raw = window.localStorage.getItem(STATE_KEY)
  if (!raw) {
    const initial = seedState()
    window.localStorage.setItem(STATE_KEY, JSON.stringify(initial))
    return initial
  }
  try {
    const parsed = JSON.parse(raw) as Po01State
    if (!Array.isArray(parsed.recepciones) || !Array.isArray(parsed.movimientos)) {
      throw new Error('Estado inválido')
    }
    return parsed
  } catch {
    const initial = seedState()
    window.localStorage.setItem(STATE_KEY, JSON.stringify(initial))
    return initial
  }
}

function writeState(state: Po01State): void {
  window.localStorage.setItem(STATE_KEY, JSON.stringify(state))
}

function readSession(): Po01Session | null {
  const raw = window.sessionStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    const session = JSON.parse(raw) as Po01Session
    if (!session?.token || !session?.usuario?.usuario) return null
    return session
  } catch {
    return null
  }
}

function currentUser(): Po01Session['usuario'] {
  const session = readSession()
  if (!session) throw new Error('La sesión no está activa. Vuelva a iniciar sesión.')
  return session.usuario
}

function requireRole(roles: Array<Po01Session['usuario']['rol']>): Po01Session['usuario'] {
  const user = currentUser()
  if (!roles.includes(user.rol)) {
    throw new Error('No tiene permiso para esta acción.')
  }
  return user
}

function decisionState(humedadPct: number): EstadoRecepcion {
  if (humedadPct > 15) return 'RECHAZADA'
  if (humedadPct > 14.5) return 'PENDIENTE'
  return 'ACEPTADA'
}

function addMovement(
  state: Po01State,
  reception: Recepcion,
  usuario: string,
): Movimiento {
  const previous = state.movimientos[state.movimientos.length - 1]
  const saldo = roundKg((previous?.saldoKg ?? 0) + (reception.entradaKardexKg ?? 0))
  const movement: Movimiento = {
    idMovimiento: state.nextMovimiento++,
    fechaHora: new Date().toISOString(),
    tipoMovimiento: 'ENTRADA',
    cantidadKg: reception.entradaKardexKg ?? 0,
    saldoKg: saldo,
    idRecepcion: reception.idRecepcion,
    usuario,
  }
  state.movimientos.push(movement)
  return movement
}

function stockKg(state: Po01State): number {
  return state.movimientos.at(-1)?.saldoKg ?? 0
}

export const resetPo01State = (): void => {
  window.localStorage.removeItem(STATE_KEY)
}

export const po01ApiMode =
  import.meta.env.VITE_USAR_MOCKS === 'true' || !import.meta.env.VITE_API_URL
    ? 'demo'
    : 'api'

export const mockPo01Api: Po01ApiClient = {
  async login(usuario: string, password: string): Promise<Po01Session> {
    const found = usuarios.find(
      (candidate) =>
        candidate.usuario.toLowerCase() === usuario.trim().toLowerCase() &&
        candidate.password === password,
    )
    if (!found) {
      throw new Error('Usuario o contraseña incorrectos.')
    }
    const session: Po01Session = {
      token: `demo-token-${found.id}-${Date.now()}`,
      usuario: {
        id: found.id,
        nombres: found.nombres,
        usuario: found.usuario,
        rol: found.rol,
      },
    }
    window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(session))
    return session
  },

  async getProveedores(): Promise<Proveedor[]> {
    currentUser()
    return proveedores
  },

  async getRecepciones(fecha?: string): Promise<Recepcion[]> {
    currentUser()
    const state = readState()
    const rows = fecha
      ? state.recepciones.filter((row) => localDate(row.fechaHora) === fecha)
      : state.recepciones
    return [...rows].sort(
      (a, b) => new Date(b.fechaHora).getTime() - new Date(a.fechaHora).getTime(),
    )
  },

  async crearRecepcion(input: RecepcionInput): Promise<Recepcion> {
    const user = requireRole(['OPERARIO', 'JEFE_PLANTA'])
    const provider = proveedores.find(
      (candidate) => candidate.idProveedor === input.idProveedor,
    )
    if (!provider) throw new Error('Seleccione un proveedor válido.')
    const bruto = Number(input.pesoBrutoKg)
    const tara = Number(input.taraKg)
    const sacos = Number(input.pesoSacosKg)
    const merma = Number(input.mermaLimpiezaKg)
    const humedad = Number(input.humedadPct)
    if (![bruto, tara, sacos, merma, humedad].every(Number.isFinite)) {
      throw new Error('Complete todos los valores numéricos.')
    }
    if (bruto <= 0) throw new Error('El peso bruto debe ser mayor a cero.')
    if (tara < 0 || sacos < 0 || merma < 0) {
      throw new Error('Los pesos y la merma no pueden ser negativos.')
    }
    if (tara >= bruto) throw new Error('La tara debe ser menor que el peso bruto.')
    if (humedad < 0 || humedad > 100) {
      throw new Error('La humedad debe estar entre 0 y 100.')
    }
    const neto = roundKg(bruto - tara - sacos)
    if (neto <= 0) {
      throw new Error('El peso neto debe ser mayor a cero.')
    }
    if (merma >= neto) {
      throw new Error('La merma de limpieza debe ser menor que el peso neto.')
    }

    const state = readState()
    const estado = decisionState(humedad)
    const reception: Recepcion = {
      idRecepcion: state.nextRecepcion++,
      fechaHora: new Date().toISOString(),
      proveedor: provider.razonSocial,
      pesoBrutoKg: bruto,
      taraKg: tara,
      pesoSacosKg: sacos,
      pesoNetoKg: neto,
      humedadPct: humedad,
      mermaLimpiezaKg: merma,
      estado,
      entradaKardexKg: estado === 'ACEPTADA' ? roundKg(neto - merma) : null,
      registradoPor: user.usuario,
      autorizadoPor: null,
    }
    state.recepciones.push(reception)
    if (reception.estado === 'ACEPTADA') {
      addMovement(state, reception, user.usuario)
    }
    writeState(state)
    return reception
  },

  async decidirRecepcion(
    idRecepcion: number,
    input: DecisionInput,
  ): Promise<Recepcion> {
    const user = requireRole(['JEFE_PLANTA'])
    if (!input.motivo.trim()) {
      throw new Error('El motivo de la decisión es obligatorio.')
    }
    if (input.decision !== 'ACEPTAR' && input.decision !== 'RECHAZAR') {
      throw new Error('Seleccione aceptar o rechazar.')
    }
    const state = readState()
    const reception = state.recepciones.find(
      (row) => row.idRecepcion === idRecepcion,
    )
    if (!reception) throw new Error('La recepción no existe.')
    if (reception.estado !== 'PENDIENTE') {
      throw new Error('La recepción ya fue atendida.')
    }
    if (input.decision === 'ACEPTAR') {
      reception.estado = 'ACEPTADA'
      reception.entradaKardexKg = roundKg(
        reception.pesoNetoKg - reception.mermaLimpiezaKg,
      )
      reception.autorizadoPor = user.usuario
      addMovement(state, reception, user.usuario)
    } else {
      reception.estado = 'RECHAZADA'
      reception.entradaKardexKg = null
      reception.autorizadoPor = null
    }
    writeState(state)
    return { ...reception }
  },

  async getKardex(): Promise<Movimiento[]> {
    currentUser()
    const state = readState()
    return [...state.movimientos].sort(
      (a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime(),
    )
  },

  async getStockMaiz(): Promise<StockMaiz> {
    currentUser()
    return { stockKg: stockKg(readState()) }
  },
}

export function fechaHoy(): string {
  return todayIso()
}
