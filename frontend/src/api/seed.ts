import type {
  Cliente,
  Despacho,
  FormulaDetalle,
  FormulaMacro,
  Insumo,
  Lote,
  Movimiento,
  Pedido,
  Producto,
  Proveedor,
  Recepcion,
  Usuario,
} from './types'

export interface DemoState {
  usuarios: Array<Usuario & { password: string }>
  proveedores: Proveedor[]
  clientes: Cliente[]
  insumos: Insumo[]
  productos: Producto[]
  formulas: FormulaDetalle[]
  recepciones: Recepcion[]
  lotes: Lote[]
  pedidos: Pedido[]
  despachos: Despacho[]
  movimientos: Movimiento[]
}

const initialDate = new Date(2026, 8, 17, 8, 0, 0)

function iso(date: Date): string {
  return date.toISOString()
}

function relativeDate(daysAgo: number, hour: number, minute: number): string {
  const date = new Date()
  date.setDate(date.getDate() - daysAgo)
  date.setHours(hour, minute, 0, 0)
  return iso(date)
}

function baseMacros(percentages: number[]): FormulaMacro[] {
  const names = [
    'Maíz',
    'Torta de soya',
    'Subproducto de trigo',
    'Aceite de palma',
    'Carbonato de calcio',
    'Sal',
  ]
  return percentages.map((pct, index) => ({
    id_insumo: index + 1,
    insumo: names[index],
    cantidad_calculada_kg: Number((pct * 10).toFixed(3)),
    cantidad_real_kg: null,
    proporcion_pct: pct,
  }))
}

const formulasBySpecies: Record<string, number[]> = {
  Cerdo: [55, 24, 12, 4, 3, 2],
  Pollo: [60, 22, 10, 4, 2.5, 1.5],
  Cuy: [50, 20, 20, 3, 4, 3],
  Conejo: [52, 18, 18, 4, 5, 3],
  Vacuno: [58, 18, 15, 2, 4, 3],
}

const productRows: Array<[string, string, string, string | null, number]> = [
  ['Cerdo granja inicio', 'Cerdo', 'Inicio', 'GJA', 22],
  ['Cerdo granja crecimiento', 'Cerdo', 'Crecimiento', 'GJA', 14],
  ['Pollo granja inicio', 'Pollo', 'Inicio', 'GJA', 18],
  ['Pollo granja crecimiento', 'Pollo', 'Crecimiento', 'GJA', 21],
  ['Pollo granja engorde', 'Pollo', 'Engorde', 'GJA', 21],
  ['Cuy engorde', 'Cuy', 'Engorde', null, 9],
  ['Conejo engorde', 'Conejo', 'Engorde', null, 2],
  ['Cerdo mercado inicio', 'Cerdo', 'Inicio', 'MCDO', 5],
  ['Cerdo mercado crecimiento', 'Cerdo', 'Crecimiento', 'MCDO', 17],
  ['Cerdo mercado engorde', 'Cerdo', 'Engorde', 'MCDO', 39],
  ['Pollo mercado inicio', 'Pollo', 'Inicio', 'MCDO', 9],
  ['Pollo mercado crecimiento', 'Pollo', 'Crecimiento', 'MCDO', 16],
  ['Pollo mercado engorde', 'Pollo', 'Engorde', 'MCDO', 14],
  ['Cerdo gestación', 'Cerdo', 'Gestación', null, 9],
  ['Engorde toro', 'Vacuno', 'Engorde', null, 13],
]

const insumoRows: Array<[string, number]> = [
  ['Maíz', 6500],
  ['Torta de soya', 1200],
  ['Subproducto de trigo', 900],
  ['Aceite de palma', 250],
  ['Carbonato de calcio', 400],
  ['Sal', 300],
]

export function createSeedState(): DemoState {
  const usuarios: DemoState['usuarios'] = [
    {
      id_usuario: 1,
      nombres: 'Administrador de prueba',
      usuario: 'admin_demo',
      rol: 'ADMINISTRADOR',
      password: 'demo123',
    },
    {
      id_usuario: 2,
      nombres: 'Jefe de planta',
      usuario: 'jefe_demo',
      rol: 'JEFE_PLANTA',
      password: 'demo123',
    },
    {
      id_usuario: 3,
      nombres: 'Operario de planta',
      usuario: 'operario_demo',
      rol: 'OPERARIO',
      password: 'demo123',
    },
    {
      id_usuario: 4,
      nombres: 'Contadora',
      usuario: 'contadora_demo',
      rol: 'CONTADORA',
      password: 'demo123',
    },
  ]

  const proveedores: Proveedor[] = [
    {
      id_proveedor: 1,
      razon_social: 'PROVEEDOR_DE_PRUEBA',
      documento: null,
      telefono: null,
    },
    {
      id_proveedor: 2,
      razon_social: 'AGROINSUMOS SAN MARTÍN',
      documento: null,
      telefono: '000 000 000',
    },
  ]

  const clientes: Cliente[] = [
    {
      id_cliente: 1,
      tipo_cliente: 'REVENDEDOR',
      nombre_razon_social: 'CLIENTE_DE_PRUEBA',
      zona: 'DATO_DE_PRUEBA',
      telefono: null,
    },
    {
      id_cliente: 2,
      tipo_cliente: 'CRIADOR_TECNIFICADO',
      nombre_razon_social: 'GRANJA DEMO',
      zona: 'Moyobamba',
      telefono: '000 000 000',
    },
  ]

  const insumos: Insumo[] = insumoRows.map(([nombre, stock], index) => ({
    id_insumo: index + 1,
    nombre,
    unidad_medida: 'kg',
    stock_kg: stock,
  }))

  const productos: Producto[] = productRows.map(
    ([nombre, especie, fase, linea, sacos], index) => ({
      id_producto: index + 1,
      nombre,
      especie,
      fase,
      linea_comercial: linea,
      presentacion: 'Saco de 40 kg',
      stock_kg: sacos * 40,
    }),
  )

  const formulas: FormulaDetalle[] = productos.map((producto) => ({
    id_formula: producto.id_producto,
    id_producto: producto.id_producto,
    producto_nombre: producto.nombre,
    version: 'v1.0-demo',
    vigente: true,
    confidencial: true,
    nota:
      'Fórmula DEMO con composición ficticia. No representa la fórmula real de Moliselva ni debe usarse para producción.',
    macros: baseMacros(
      formulasBySpecies[producto.especie] ?? [55, 24, 12, 4, 3, 2],
    ),
  }))

  const movimientos: Movimiento[] = []
  let movimientoId = 1

  productos.forEach((producto) => {
    movimientos.push({
      id_movimiento: movimientoId++,
      fecha_hora: iso(initialDate),
      tipo_movimiento: 'AJUSTE_ENTRADA',
      tipo_item: 'PRODUCTO',
      id_item: producto.id_producto,
      item: producto.nombre,
      cantidad: producto.stock_kg,
      unidad_medida: 'kg',
      referencia: 'Carga inicial 17-09-2026',
      id_usuario: 1,
      usuario_nombre: 'Administrador de prueba',
      saldo: 0,
    })
  })

  insumos.forEach((insumo) => {
    movimientos.push({
      id_movimiento: movimientoId++,
      fecha_hora: iso(new Date(initialDate.getTime() + 15 * 60 * 1000)),
      tipo_movimiento: 'AJUSTE_ENTRADA',
      tipo_item: 'INSUMO',
      id_item: insumo.id_insumo,
      item: insumo.nombre,
      cantidad: insumo.stock_kg,
      unidad_medida: 'kg',
      referencia: 'Saldo inicial demo',
      id_usuario: 1,
      usuario_nombre: 'Administrador de prueba',
      saldo: 0,
    })
  })

  const recepcionAceptada: Recepcion = {
    id_recepcion: 1,
    id_proveedor: 1,
    proveedor_nombre: 'PROVEEDOR_DE_PRUEBA',
    id_insumo: 1,
    insumo_nombre: 'Maíz',
    fecha_hora: relativeDate(1, 16, 30),
    peso_bruto_kg: 2060,
    tara_kg: 30,
    peso_sacos_kg: 30,
    peso_neto_kg: 2000,
    humedad_pct: 13.8,
    estado: 'ACEPTADA',
    id_autoriza: null,
    autoriza_nombre: null,
  }

  const recepcionPendiente: Recepcion = {
    id_recepcion: 2,
    id_proveedor: 1,
    proveedor_nombre: 'PROVEEDOR_DE_PRUEBA',
    id_insumo: 1,
    insumo_nombre: 'Maíz',
    fecha_hora: relativeDate(0, 9, 15),
    peso_bruto_kg: 3260,
    tara_kg: 30,
    peso_sacos_kg: 30,
    peso_neto_kg: 3200,
    humedad_pct: 14.8,
    estado: 'PENDIENTE',
    id_autoriza: null,
    autoriza_nombre: null,
  }

  movimientos.push({
    id_movimiento: movimientoId++,
    fecha_hora: recepcionAceptada.fecha_hora,
    tipo_movimiento: 'ENTRADA',
    tipo_item: 'INSUMO',
    id_item: 1,
    item: 'Maíz',
    cantidad: recepcionAceptada.peso_neto_kg,
    unidad_medida: 'kg',
    referencia: 'Recepción #1',
    id_usuario: 3,
    usuario_nombre: 'Operario de planta',
    saldo: 0,
  })

  const maizInicial = insumos.find((item) => item.id_insumo === 1)
  if (maizInicial) {
    maizInicial.stock_kg = Number(
      (maizInicial.stock_kg + recepcionAceptada.peso_neto_kg).toFixed(2),
    )
  }

  const formulasById = new Map(formulas.map((formula) => [formula.id_formula, formula]))

  function macrosParaLote(idFormula: number, tamanoKg: number): FormulaMacro[] {
    const formula = formulasById.get(idFormula)
    if (!formula) return []
    return formula.macros.map((macro) => ({
      ...macro,
      cantidad_calculada_kg: Number(
        ((macro.proporcion_pct / 100) * tamanoKg).toFixed(3),
      ),
      cantidad_real_kg: null,
    }))
  }

  const lotes: Lote[] = [
    {
      id_lote: 1,
      id_producto: 1,
      producto_nombre: 'Cerdo granja inicio',
      id_formula: 1,
      formula_version: 'v1.0-demo',
      fecha_hora: relativeDate(0, 8, 10),
      tamano_kg: 500,
      estado: 'PLANIFICADO',
      id_responsable: 2,
      responsable_nombre: 'Jefe de planta',
      macros: macrosParaLote(1, 500),
    },
    {
      id_lote: 2,
      id_producto: 5,
      producto_nombre: 'Pollo granja engorde',
      id_formula: 5,
      formula_version: 'v1.0-demo',
      fecha_hora: relativeDate(0, 7, 30),
      tamano_kg: 1000,
      estado: 'EN_PROCESO',
      id_responsable: 3,
      responsable_nombre: 'Operario de planta',
      macros: macrosParaLote(5, 1000),
    },
    {
      id_lote: 3,
      id_producto: 6,
      producto_nombre: 'Cuy engorde',
      id_formula: 6,
      formula_version: 'v1.0-demo',
      fecha_hora: relativeDate(0, 10, 20),
      tamano_kg: 500,
      estado: 'PLANIFICADO',
      id_responsable: 3,
      responsable_nombre: 'Operario de planta',
      macros: macrosParaLote(6, 500),
    },
  ]

  const pedidos: Pedido[] = [
    {
      id_pedido: 1,
      id_cliente: 1,
      cliente_nombre: 'CLIENTE_DE_PRUEBA',
      fecha_hora: relativeDate(0, 9, 40),
      estado: 'PENDIENTE',
      observacion: 'Pedido demo: retiro en planta',
      items: [
        {
          id_producto: 1,
          producto_nombre: 'Cerdo granja inicio',
          cantidad_sacos: 5,
          cantidad_kg: 200,
        },
        {
          id_producto: 5,
          producto_nombre: 'Pollo granja engorde',
          cantidad_sacos: 3,
          cantidad_kg: 120,
        },
      ],
    },
    {
      id_pedido: 2,
      id_cliente: 2,
      cliente_nombre: 'GRANJA DEMO',
      fecha_hora: relativeDate(0, 10, 5),
      estado: 'PENDIENTE',
      observacion: 'Entrega programada en Moyobamba',
      items: [
        {
          id_producto: 6,
          producto_nombre: 'Cuy engorde',
          cantidad_sacos: 2,
          cantidad_kg: 80,
        },
        {
          id_producto: 10,
          producto_nombre: 'Cerdo mercado engorde',
          cantidad_sacos: 3,
          cantidad_kg: 120,
        },
      ],
    },
  ]

  const despachos: Despacho[] = [
    {
      id_despacho: 1,
      id_pedido: 1,
      cliente_nombre: 'CLIENTE_DE_PRUEBA',
      fecha_hora: relativeDate(0, 9, 45),
      tipo_entrega: 'RECOJO_PLANTA',
      destino: null,
      estado: 'BORRADOR',
      id_usuario: 2,
      usuario_nombre: 'Jefe de planta',
      items: pedidos[0].items,
    },
  ]

  return {
    usuarios,
    proveedores,
    clientes,
    insumos,
    productos,
    formulas,
    recepciones: [recepcionAceptada, recepcionPendiente],
    lotes,
    pedidos,
    despachos,
    movimientos,
  }
}
