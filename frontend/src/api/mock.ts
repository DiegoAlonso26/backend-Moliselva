import { createSeedState, type DemoState } from './seed'
import type {
  ApiClient,
  DashboardData,
  Despacho,
  DespachoInput,
  FormulaDetalle,
  FormulaMacro,
  Lote,
  LoteInput,
  LoteRealInput,
  Movimiento,
  Pedido,
  PedidoInput,
  Recepcion,
  RecepcionInput,
  Rol,
  Session,
  StockResumen,
  Usuario,
} from './types'

const STORAGE_KEY = 'moliselva_demo_state_v2'
const SESSION_KEY = 'moliselva_session'

function delay(ms = 220): Promise<void> {
  return new Promise((resolve) => window.setTimeout(resolve, ms))
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T
}

function loadState(): DemoState {
  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    const state = createSeedState()
    saveState(state)
    return state
  }
  try {
    return JSON.parse(raw) as DemoState
  } catch {
    const state = createSeedState()
    saveState(state)
    return state
  }
}

function saveState(state: DemoState): void {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

function getSession(): Session | null {
  const raw = window.localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    const session = JSON.parse(raw) as Session
    if (!session?.usuario?.id_usuario || !session.usuario.rol) return null
    return session
  } catch {
    return null
  }
}

function requireUser(): Usuario {
  const session = getSession()
  if (!session) throw new Error('Debe iniciar sesión para continuar.')
  return session.usuario
}

function requireRole(roles: Rol[]): Usuario {
  const user = requireUser()
  if (!roles.includes(user.rol)) {
    throw new Error('Su rol no tiene permiso para realizar esta acción.')
  }
  return user
}

function nextId<T, K extends keyof T>(rows: T[], key: K): number {
  return (
    rows.reduce((max, row) => {
      const value = Number(row[key])
      return Number.isFinite(value) && value > max ? value : max
    }, 0) + 1
  )
}

function addMovement(
  state: DemoState,
  data: {
    fecha_hora: string
    tipo_movimiento: Movimiento['tipo_movimiento']
    tipo_item: Movimiento['tipo_item']
    id_item: number
    item: string
    cantidad: number
    unidad_medida: string
    referencia: string | null
    user: Usuario
  },
): void {
  state.movimientos.push({
    id_movimiento: nextId(state.movimientos, 'id_movimiento'),
    fecha_hora: data.fecha_hora,
    tipo_movimiento: data.tipo_movimiento,
    tipo_item: data.tipo_item,
    id_item: data.id_item,
    item: data.item,
    cantidad: data.cantidad,
    unidad_medida: data.unidad_medida,
    referencia: data.referencia,
    id_usuario: data.user.id_usuario,
    usuario_nombre: data.user.nombres,
    saldo: 0,
  })
}

function buildKardex(state: DemoState): Movimiento[] {
  const sorted = [...state.movimientos].sort((a, b) => {
    const dateCompare = a.fecha_hora.localeCompare(b.fecha_hora)
    return dateCompare !== 0 ? dateCompare : a.id_movimiento - b.id_movimiento
  })
  const balances = new Map<string, number>()
  const withBalance = sorted.map((movement) => {
    const key = `${movement.tipo_item}-${movement.id_item}`
    const current = balances.get(key) ?? 0
    const delta =
      movement.tipo_movimiento === 'ENTRADA' ||
      movement.tipo_movimiento === 'AJUSTE_ENTRADA'
        ? movement.cantidad
        : -movement.cantidad
    const saldo = Number((current + delta).toFixed(3))
    balances.set(key, saldo)
    return { ...movement, saldo }
  })
  return withBalance.reverse()
}

function buildStock(state: DemoState): StockResumen {
  return {
    productos: clone(state.productos),
    insumos: clone(state.insumos),
    total_producto_kg: Number(
      state.productos.reduce((total, product) => total + product.stock_kg, 0).toFixed(2),
    ),
    total_sacos: state.productos.reduce(
      (total, product) => total + Math.floor(product.stock_kg / 40),
      0,
    ),
  }
}


function macrosForSize(formula: FormulaDetalle, tamanoKg: number): FormulaMacro[] {
  return formula.macros.map((macro) => ({
    ...macro,
    cantidad_calculada_kg: Number(
      ((macro.proporcion_pct / 100) * tamanoKg).toFixed(3),
    ),
    cantidad_real_kg: null,
  }))
}

function hideComposition(macros: FormulaMacro[]): FormulaMacro[] {
  return macros.map((macro) => ({ ...macro, proporcion_pct: 0 }))
}

function visibleLote(lote: Lote, user: Usuario): Lote {
  if (user.rol === 'ADMINISTRADOR' || user.rol === 'JEFE_PLANTA') {
    return clone(lote)
  }
  return { ...clone(lote), macros: hideComposition(lote.macros) }
}

function findFormula(state: DemoState, idFormula: number): FormulaDetalle {
  const formula = state.formulas.find((row) => row.id_formula === idFormula)
  if (!formula) throw new Error('La fórmula indicada no existe.')
  return formula
}

function findInsumo(state: DemoState, idInsumo: number) {
  const insumo = state.insumos.find((row) => row.id_insumo === idInsumo)
  if (!insumo) throw new Error('El insumo indicado no existe.')
  return insumo
}

function findProducto(state: DemoState, idProducto: number) {
  const producto = state.productos.find((row) => row.id_producto === idProducto)
  if (!producto) throw new Error('El producto indicado no existe.')
  return producto
}

function sameLocalDate(isoDate: string, date: Date): boolean {
  const value = new Date(isoDate)
  return (
    value.getFullYear() === date.getFullYear() &&
    value.getMonth() === date.getMonth() &&
    value.getDate() === date.getDate()
  )
}

export const mockApi: ApiClient = {
  async login(usuario, password): Promise<Session> {
    await delay()
    const state = loadState()
    const found = state.usuarios.find(
      (row) =>
        row.usuario.toLowerCase() === usuario.trim().toLowerCase() &&
        row.password === password,
    )
    if (!found) {
      throw new Error('Usuario o contraseña incorrectos.')
    }
    const user: Usuario = {
      id_usuario: found.id_usuario,
      nombres: found.nombres,
      usuario: found.usuario,
      rol: found.rol,
    }
    const session: Session = {
      token: `demo-${user.id_usuario}-${Date.now()}`,
      usuario: user,
    }
    window.localStorage.setItem(SESSION_KEY, JSON.stringify(session))
    return session
  },

  async getDashboard(): Promise<DashboardData> {
    await delay(120)
    requireUser()
    const state = loadState()
    const kardex = buildKardex(state)
    const today = new Date()
    return {
      stock: buildStock(state),
      recepciones_hoy: state.recepciones.filter((row) =>
        sameLocalDate(row.fecha_hora, today),
      ).length,
      lotes_en_proceso: state.lotes.filter((row) => row.estado === 'EN_PROCESO').length,
      pedidos_pendientes: state.pedidos.filter((row) => row.estado === 'PENDIENTE').length,
      movimientos: kardex.slice(0, 7),
      alertas: state.productos.filter((row) => row.stock_kg <= 80),
    }
  },

  async getProveedores() {
    await delay(100)
    requireUser()
    return clone(loadState().proveedores)
  },

  async getClientes() {
    await delay(100)
    requireUser()
    return clone(loadState().clientes)
  },

  async getInsumos() {
    await delay(100)
    requireUser()
    return clone(loadState().insumos)
  },

  async getProductos() {
    await delay(100)
    requireUser()
    return clone(loadState().productos)
  },

  async getFormulas() {
    await delay(100)
    requireUser()
    return loadState().formulas.map((formula) => ({
      id_formula: formula.id_formula,
      id_producto: formula.id_producto,
      producto_nombre: formula.producto_nombre,
      version: formula.version,
      vigente: formula.vigente,
      confidencial: formula.confidencial,
    }))
  },

  async getFormula(idFormula, tamanoKg): Promise<FormulaDetalle> {
    await delay(100)
    const user = requireUser()
    const formula = findFormula(loadState(), idFormula)
    const macros = macrosForSize(formula, tamanoKg)
    const restricted = user.rol === 'OPERARIO' || user.rol === 'CONTADORA'
    return {
      ...clone(formula),
      macros: restricted ? hideComposition(macros) : macros,
      nota: restricted
        ? 'Composición reservada. Solo se muestran las cantidades necesarias para el lote.'
        : formula.nota,
    }
  },

  async getRecepciones() {
    await delay(120)
    requireUser()
    return clone(loadState().recepciones).sort((a, b) =>
      b.fecha_hora.localeCompare(a.fecha_hora),
    )
  },

  async crearRecepcion(input: RecepcionInput): Promise<Recepcion> {
    await delay(260)
    const user = requireRole(['ADMINISTRADOR', 'JEFE_PLANTA', 'OPERARIO'])
    const state = loadState()
    const proveedor = state.proveedores.find(
      (row) => row.id_proveedor === input.id_proveedor,
    )
    if (!proveedor) throw new Error('Seleccione un proveedor válido.')
    const insumo = findInsumo(state, input.id_insumo)
    const pesoSacos = Math.max(0, Number(input.peso_sacos_kg) || 0)
    const tara = Math.max(0, Number(input.tara_kg) || 0)
    const bruto = Number(input.peso_bruto_kg) || 0
    const neto = Number((bruto - tara - pesoSacos).toFixed(2))
    const humedad = Number(input.humedad_pct)
    if (bruto <= 0) throw new Error('El peso bruto debe ser mayor a cero.')
    if (neto <= 0) throw new Error('El peso neto debe ser mayor a cero.')
    if (!Number.isFinite(humedad) || humedad <= 0 || humedad > 100) {
      throw new Error('Ingrese una humedad válida entre 0 y 100%.')
    }

    let estado: Recepcion['estado'] = 'ACEPTADA'
    let idAutoriza: number | null = null
    let autorizaNombre: string | null = null
    if (humedad > 15) {
      estado = 'RECHAZADA'
    } else if (humedad > 14.5) {
      const canAuthorize =
        input.autorizar &&
        (user.rol === 'ADMINISTRADOR' || user.rol === 'JEFE_PLANTA')
      if (canAuthorize) {
        idAutoriza = user.id_usuario
        autorizaNombre = user.nombres
      } else {
        estado = 'PENDIENTE'
      }
    }

    const recepcion: Recepcion = {
      id_recepcion: nextId(state.recepciones, 'id_recepcion'),
      id_proveedor: proveedor.id_proveedor,
      proveedor_nombre: proveedor.razon_social,
      id_insumo: insumo.id_insumo,
      insumo_nombre: insumo.nombre,
      fecha_hora: new Date().toISOString(),
      peso_bruto_kg: bruto,
      tara_kg: tara,
      peso_sacos_kg: pesoSacos,
      peso_neto_kg: neto,
      humedad_pct: humedad,
      estado,
      id_autoriza: idAutoriza,
      autoriza_nombre: autorizaNombre,
    }

    if (estado === 'ACEPTADA') {
      insumo.stock_kg = Number((insumo.stock_kg + neto).toFixed(2))
      addMovement(state, {
        fecha_hora: recepcion.fecha_hora,
        tipo_movimiento: 'ENTRADA',
        tipo_item: 'INSUMO',
        id_item: insumo.id_insumo,
        item: insumo.nombre,
        cantidad: neto,
        unidad_medida: insumo.unidad_medida,
        referencia: `Recepción #${recepcion.id_recepcion}`,
        user,
      })
    }

    state.recepciones.push(recepcion)
    saveState(state)
    return clone(recepcion)
  },

  async autorizarRecepcion(idRecepcion): Promise<Recepcion> {
    await delay(220)
    const user = requireRole(['ADMINISTRADOR', 'JEFE_PLANTA'])
    const state = loadState()
    const recepcion = state.recepciones.find(
      (row) => row.id_recepcion === idRecepcion,
    )
    if (!recepcion) throw new Error('La recepción indicada no existe.')
    if (recepcion.estado !== 'PENDIENTE') {
      throw new Error('Solo se pueden autorizar recepciones pendientes.')
    }
    const insumo = findInsumo(state, recepcion.id_insumo)
    recepcion.estado = 'ACEPTADA'
    recepcion.id_autoriza = user.id_usuario
    recepcion.autoriza_nombre = user.nombres
    insumo.stock_kg = Number(
      (insumo.stock_kg + recepcion.peso_neto_kg).toFixed(2),
    )
    addMovement(state, {
      fecha_hora: new Date().toISOString(),
      tipo_movimiento: 'ENTRADA',
      tipo_item: 'INSUMO',
      id_item: insumo.id_insumo,
      item: insumo.nombre,
      cantidad: recepcion.peso_neto_kg,
      unidad_medida: insumo.unidad_medida,
      referencia: `Recepción #${recepcion.id_recepcion} autorizada`,
      user,
    })
    saveState(state)
    return clone(recepcion)
  },

  async getLotes() {
    await delay(120)
    const user = requireUser()
    return loadState()
      .lotes.map((lote) => visibleLote(lote, user))
      .sort((a, b) => b.fecha_hora.localeCompare(a.fecha_hora))
  },

  async crearLote(input: LoteInput): Promise<Lote> {
    await delay(260)
    const user = requireRole(['ADMINISTRADOR', 'JEFE_PLANTA', 'OPERARIO'])
    const state = loadState()
    const producto = findProducto(state, input.id_producto)
    const formula = findFormula(state, input.id_formula)
    if (formula.id_producto !== producto.id_producto) {
      throw new Error('La fórmula no corresponde al producto seleccionado.')
    }
    const tamano = Number(input.tamano_kg)
    if (!Number.isFinite(tamano) || tamano <= 0) {
      throw new Error('El tamaño del lote debe ser mayor a cero.')
    }
    const lote: Lote = {
      id_lote: nextId(state.lotes, 'id_lote'),
      id_producto: producto.id_producto,
      producto_nombre: producto.nombre,
      id_formula: formula.id_formula,
      formula_version: formula.version,
      fecha_hora: new Date().toISOString(),
      tamano_kg: Number(tamano.toFixed(2)),
      estado: 'PLANIFICADO',
      id_responsable: user.id_usuario,
      responsable_nombre: user.nombres,
      macros: macrosForSize(formula, tamano),
    }
    state.lotes.push(lote)
    saveState(state)
    return visibleLote(lote, user)
  },

  async confirmarNucleo(idLote): Promise<Lote> {
    await delay(220)
    const user = requireRole(['ADMINISTRADOR', 'JEFE_PLANTA'])
    const state = loadState()
    const lote = state.lotes.find((row) => row.id_lote === idLote)
    if (!lote) throw new Error('El lote indicado no existe.')
    if (lote.estado !== 'PLANIFICADO') {
      throw new Error('Solo se puede confirmar el núcleo de un lote planificado.')
    }
    lote.macros.forEach((macro) => {
      const insumo = findInsumo(state, macro.id_insumo)
      if (insumo.stock_kg < macro.cantidad_calculada_kg) {
        throw new Error(
          `Stock insuficiente de ${insumo.nombre}. Disponible: ${insumo.stock_kg.toFixed(2)} kg.`,
        )
      }
    })
    lote.estado = 'EN_PROCESO'
    saveState(state)
    return visibleLote(lote, user)
  },

  async terminarLote(idLote, input: LoteRealInput): Promise<Lote> {
    await delay(300)
    const user = requireRole(['ADMINISTRADOR', 'JEFE_PLANTA', 'OPERARIO'])
    const state = loadState()
    const lote = state.lotes.find((row) => row.id_lote === idLote)
    if (!lote) throw new Error('El lote indicado no existe.')
    if (lote.estado !== 'EN_PROCESO') {
      throw new Error('Primero confirme el núcleo del lote para iniciar la producción.')
    }
    const realByInsumo = new Map(
      input.macros.map((row) => [row.id_insumo, Number(row.cantidad_real_kg)]),
    )
    const realMacros = lote.macros.map((macro) => {
      const real = realByInsumo.get(macro.id_insumo)
      return {
        ...macro,
        cantidad_real_kg:
          real !== undefined && Number.isFinite(real) && real >= 0
            ? Number(real.toFixed(3))
            : macro.cantidad_calculada_kg,
      }
    })
    realMacros.forEach((macro) => {
      if (macro.cantidad_real_kg === null || macro.cantidad_real_kg <= 0) {
        throw new Error(`Registre el consumo real de ${macro.insumo}.`)
      }
      const insumo = findInsumo(state, macro.id_insumo)
      if (insumo.stock_kg < macro.cantidad_real_kg) {
        throw new Error(
          `Stock insuficiente de ${insumo.nombre}. Disponible: ${insumo.stock_kg.toFixed(2)} kg.`,
        )
      }
    })
    const fecha = new Date().toISOString()
    realMacros.forEach((macro) => {
      const insumo = findInsumo(state, macro.id_insumo)
      const cantidad = macro.cantidad_real_kg ?? 0
      insumo.stock_kg = Number((insumo.stock_kg - cantidad).toFixed(2))
      addMovement(state, {
        fecha_hora: fecha,
        tipo_movimiento: 'SALIDA',
        tipo_item: 'INSUMO',
        id_item: insumo.id_insumo,
        item: insumo.nombre,
        cantidad,
        unidad_medida: insumo.unidad_medida,
        referencia: `Lote #${lote.id_lote}`,
        user,
      })
    })
    const producto = findProducto(state, lote.id_producto)
    producto.stock_kg = Number((producto.stock_kg + lote.tamano_kg).toFixed(2))
    addMovement(state, {
      fecha_hora: fecha,
      tipo_movimiento: 'ENTRADA',
      tipo_item: 'PRODUCTO',
      id_item: producto.id_producto,
      item: producto.nombre,
      cantidad: lote.tamano_kg,
      unidad_medida: 'kg',
      referencia: `Lote #${lote.id_lote} terminado`,
      user,
    })
    lote.macros = realMacros
    lote.estado = 'TERMINADO'
    saveState(state)
    return visibleLote(lote, user)
  },

  async getStock(): Promise<StockResumen> {
    await delay(100)
    requireUser()
    return buildStock(loadState())
  },

  async getKardex(): Promise<Movimiento[]> {
    await delay(120)
    requireUser()
    return buildKardex(loadState())
  },

  async getPedidos() {
    await delay(120)
    requireUser()
    return clone(loadState().pedidos).sort((a, b) =>
      b.fecha_hora.localeCompare(a.fecha_hora),
    )
  },

  async crearPedido(input: PedidoInput): Promise<Pedido> {
    await delay(260)
    requireRole(['ADMINISTRADOR', 'JEFE_PLANTA', 'CONTADORA'])
    const state = loadState()
    const cliente = state.clientes.find((row) => row.id_cliente === input.id_cliente)
    if (!cliente) throw new Error('Seleccione un cliente válido.')
    if (!input.items.length) throw new Error('Agregue al menos un producto al pedido.')
    const items = input.items.map((row) => {
      const producto = findProducto(state, row.id_producto)
      const sacos = Number(row.cantidad_sacos)
      if (!Number.isFinite(sacos) || sacos <= 0) {
        throw new Error(`La cantidad de ${producto.nombre} debe ser mayor a cero.`)
      }
      const kg = Number((sacos * 40).toFixed(2))
      if (producto.stock_kg < kg) {
        throw new Error(
          `Stock insuficiente de ${producto.nombre}. Disponible: ${producto.stock_kg.toFixed(2)} kg.`,
        )
      }
      return {
        id_producto: producto.id_producto,
        producto_nombre: producto.nombre,
        cantidad_sacos: sacos,
        cantidad_kg: kg,
      }
    })
    const pedido: Pedido = {
      id_pedido: nextId(state.pedidos, 'id_pedido'),
      id_cliente: cliente.id_cliente,
      cliente_nombre: cliente.nombre_razon_social,
      fecha_hora: new Date().toISOString(),
      estado: 'PENDIENTE',
      observacion: input.observacion?.trim() || null,
      items,
    }
    state.pedidos.push(pedido)
    saveState(state)
    return clone(pedido)
  },

  async getDespachos() {
    await delay(120)
    requireUser()
    return clone(loadState().despachos).sort((a, b) =>
      b.fecha_hora.localeCompare(a.fecha_hora),
    )
  },

  async crearDespacho(input: DespachoInput): Promise<Despacho> {
    await delay(260)
    const user = requireRole(['ADMINISTRADOR', 'JEFE_PLANTA', 'CONTADORA'])
    const state = loadState()
    const pedido = state.pedidos.find((row) => row.id_pedido === input.id_pedido)
    if (!pedido) throw new Error('El pedido indicado no existe.')
    if (pedido.estado === 'ATENDIDO' || pedido.estado === 'CANCELADO') {
      throw new Error('El pedido ya no está disponible para despacho.')
    }
    const active = state.despachos.find(
      (row) =>
        row.id_pedido === pedido.id_pedido &&
        (row.estado === 'BORRADOR' || row.estado === 'CONFIRMADO'),
    )
    if (active) {
      throw new Error('El pedido ya tiene un despacho en borrador o confirmado.')
    }
    const despacho: Despacho = {
      id_despacho: nextId(state.despachos, 'id_despacho'),
      id_pedido: pedido.id_pedido,
      cliente_nombre: pedido.cliente_nombre,
      fecha_hora: new Date().toISOString(),
      tipo_entrega: input.tipo_entrega,
      destino: input.destino?.trim() || null,
      estado: 'BORRADOR',
      id_usuario: user.id_usuario,
      usuario_nombre: user.nombres,
      items: clone(pedido.items),
    }
    state.despachos.push(despacho)
    saveState(state)
    return clone(despacho)
  },

  async confirmarDespacho(idDespacho): Promise<Despacho> {
    await delay(300)
    const user = requireRole([
      'ADMINISTRADOR',
      'JEFE_PLANTA',
      'CONTADORA',
      'OPERARIO',
    ])
    const state = loadState()
    const despacho = state.despachos.find(
      (row) => row.id_despacho === idDespacho,
    )
    if (!despacho) throw new Error('El despacho indicado no existe.')
    if (despacho.estado !== 'BORRADOR') {
      throw new Error('Solo se puede confirmar un despacho en borrador.')
    }
    despacho.items.forEach((item) => {
      const producto = findProducto(state, item.id_producto)
      if (producto.stock_kg < item.cantidad_kg) {
        throw new Error(
          `Stock insuficiente de ${producto.nombre}. Disponible: ${producto.stock_kg.toFixed(2)} kg; requerido: ${item.cantidad_kg.toFixed(2)} kg.`,
        )
      }
    })
    const fecha = new Date().toISOString()
    despacho.items.forEach((item) => {
      const producto = findProducto(state, item.id_producto)
      producto.stock_kg = Number(
        (producto.stock_kg - item.cantidad_kg).toFixed(2),
      )
      addMovement(state, {
        fecha_hora: fecha,
        tipo_movimiento: 'SALIDA',
        tipo_item: 'PRODUCTO',
        id_item: producto.id_producto,
        item: producto.nombre,
        cantidad: item.cantidad_kg,
        unidad_medida: 'kg',
        referencia: `Despacho #${despacho.id_despacho} · Pedido #${despacho.id_pedido}`,
        user,
      })
    })
    despacho.estado = 'CONFIRMADO'
    const pedido = state.pedidos.find((row) => row.id_pedido === despacho.id_pedido)
    if (pedido) pedido.estado = 'ATENDIDO'
    saveState(state)
    return clone(despacho)
  },
}

export function resetDemoState(): void {
  saveState(createSeedState())
}
