// @vitest-environment happy-dom
import { beforeEach, describe, expect, test, vi } from 'vitest'
import { mockApi, resetDemoState } from '../api/mock'

vi.setConfig({ testTimeout: 30000 })

async function loginAs(usuario: string) {
  await mockApi.login(usuario, 'demo123')
}

async function stockOfInsumo(idInsumo: number) {
  const stock = await mockApi.getStock()
  return stock.insumos.find((row) => row.id_insumo === idInsumo)?.stock_kg ?? 0
}

async function stockOfProducto(idProducto: number) {
  const stock = await mockApi.getStock()
  return stock.productos.find((row) => row.id_producto === idProducto)?.stock_kg ?? 0
}

beforeEach(() => {
  window.localStorage.clear()
  resetDemoState()
})

describe('recepción de maíz', () => {
  test('acepta humedad menor o igual a 14.5% y actualiza stock', async () => {
    await loginAs('admin_demo')
    expect(await stockOfInsumo(1)).toBe(8500)

    const reception = await mockApi.crearRecepcion({
      id_proveedor: 1,
      id_insumo: 1,
      peso_bruto_kg: 1060,
      tara_kg: 30,
      peso_sacos_kg: 30,
      humedad_pct: 13.8,
      autorizar: false,
    })

    expect(reception.estado).toBe('ACEPTADA')
    expect(reception.peso_neto_kg).toBe(1000)
    expect(await stockOfInsumo(1)).toBe(9500)
  })

  test('deja pendiente la humedad entre 14.5% y 15% y exige jefe para autorizar', async () => {
    await loginAs('operario_demo')
    const pending = await mockApi.crearRecepcion({
      id_proveedor: 1,
      id_insumo: 1,
      peso_bruto_kg: 1060,
      tara_kg: 30,
      peso_sacos_kg: 30,
      humedad_pct: 14.8,
      autorizar: true,
    })
    expect(pending.estado).toBe('PENDIENTE')
    expect(await stockOfInsumo(1)).toBe(8500)

    await expect(
      mockApi.autorizarRecepcion(pending.id_recepcion),
    ).rejects.toThrow(/permiso/i)

    await loginAs('jefe_demo')
    const authorized = await mockApi.autorizarRecepcion(pending.id_recepcion)
    expect(authorized.estado).toBe('ACEPTADA')
    expect(authorized.autoriza_nombre).toBe('Jefe de planta')
    expect(await stockOfInsumo(1)).toBe(9500)
  })

  test('rechaza humedad mayor a 15% sin mover stock', async () => {
    await loginAs('admin_demo')
    const rejected = await mockApi.crearRecepcion({
      id_proveedor: 1,
      id_insumo: 1,
      peso_bruto_kg: 1060,
      tara_kg: 30,
      peso_sacos_kg: 30,
      humedad_pct: 15.2,
      autorizar: true,
    })

    expect(rejected.estado).toBe('RECHAZADA')
    expect(await stockOfInsumo(1)).toBe(8500)
  })
})

describe('producción por lote', () => {
  test('descuenta insumos reales e ingresa producto terminado', async () => {
    await loginAs('admin_demo')
    const maizAntes = await stockOfInsumo(1)
    const productoAntes = await stockOfProducto(1)

    const lote = await mockApi.crearLote({
      id_producto: 1,
      id_formula: 1,
      tamano_kg: 100,
    })
    expect(lote.estado).toBe('PLANIFICADO')
    expect(lote.macros.reduce((sum, row) => sum + row.cantidad_calculada_kg, 0)).toBe(100)

    const inProcess = await mockApi.confirmarNucleo(lote.id_lote)
    expect(inProcess.estado).toBe('EN_PROCESO')

    const finished = await mockApi.terminarLote(lote.id_lote, {
      macros: lote.macros.map((macro) => ({
        id_insumo: macro.id_insumo,
        cantidad_real_kg: macro.cantidad_calculada_kg,
      })),
    })

    expect(finished.estado).toBe('TERMINADO')
    expect(await stockOfInsumo(1)).toBe(maizAntes - 55)
    expect(await stockOfProducto(1)).toBe(productoAntes + 100)
  })

  test('oculta la composición porcentual al operario', async () => {
    await loginAs('operario_demo')
    const formula = await mockApi.getFormula(1, 500)
    expect(formula.macros.every((macro) => macro.proporcion_pct === 0)).toBe(true)
    expect(formula.macros.find((macro) => macro.insumo === 'Maíz')?.cantidad_calculada_kg).toBe(275)

    const lotes = await mockApi.getLotes()
    expect(
      lotes.every((lote) =>
        lote.macros.every((macro) => macro.proporcion_pct === 0),
      ),
    ).toBe(true)
  })
})

describe('despacho', () => {
  test('valida stock al confirmar y cierra el pedido', async () => {
    await loginAs('admin_demo')
    const producto1Antes = await stockOfProducto(1)
    const producto5Antes = await stockOfProducto(5)

    const confirmed = await mockApi.confirmarDespacho(1)
    expect(confirmed.estado).toBe('CONFIRMADO')
    expect(await stockOfProducto(1)).toBe(producto1Antes - 200)
    expect(await stockOfProducto(5)).toBe(producto5Antes - 120)

    const pedidos = await mockApi.getPedidos()
    expect(pedidos.find((row) => row.id_pedido === 1)?.estado).toBe('ATENDIDO')
    await expect(mockApi.confirmarDespacho(1)).rejects.toThrow(/borrador/i)
  })

  test('impide pedidos sin stock suficiente', async () => {
    await loginAs('admin_demo')
    await expect(
      mockApi.crearPedido({
        id_cliente: 1,
        items: [{ id_producto: 7, cantidad_sacos: 5 }],
      }),
    ).rejects.toThrow(/stock insuficiente/i)
  })

  test('el operario no puede crear pedidos ni autorizar recepciones', async () => {
    await loginAs('operario_demo')
    await expect(
      mockApi.crearPedido({
        id_cliente: 1,
        items: [{ id_producto: 1, cantidad_sacos: 1 }],
      }),
    ).rejects.toThrow(/permiso/i)
    await expect(mockApi.autorizarRecepcion(2)).rejects.toThrow(/permiso/i)
  })
})
