import { beforeEach, describe, expect, it } from 'vitest'
import { mockPo01Api, resetPo01State } from '../po01/mock'
import {
  canExportKardex,
  canRegisterReception,
  homeForRole,
} from '../po01/permissions'

const baseInput = {
  idProveedor: 1,
  pesoBrutoKg: 32500,
  taraKg: 12500,
  pesoSacosKg: 0,
  humedadPct: 14.2,
  mermaLimpiezaKg: 150,
}

async function loginAs(usuario: string) {
  return mockPo01Api.login(usuario, 'demo123')
}

beforeEach(() => {
  window.localStorage.clear()
  window.sessionStorage.clear()
  resetPo01State()
})

describe('PO-01 recepción de maíz', () => {
  it('acepta 14.2% y genera una sola entrada al kardex', async () => {
    await loginAs('operario_demo')
    const reception = await mockPo01Api.crearRecepcion(baseInput)
    expect(reception.estado).toBe('ACEPTADA')
    expect(reception.pesoNetoKg).toBe(20000)
    expect(reception.entradaKardexKg).toBe(19850)
    expect((await mockPo01Api.getStockMaiz()).stockKg).toBe(28350)
    expect(await mockPo01Api.getKardex()).toHaveLength(2)
  })

  it('deja pendiente 14.8% sin mover el kardex', async () => {
    await loginAs('operario_demo')
    const reception = await mockPo01Api.crearRecepcion({
      ...baseInput,
      humedadPct: 14.8,
    })
    expect(reception.estado).toBe('PENDIENTE')
    expect(reception.entradaKardexKg).toBeNull()
    expect((await mockPo01Api.getStockMaiz()).stockKg).toBe(8500)
  })

  it('rechaza 16% sin mover el kardex', async () => {
    await loginAs('operario_demo')
    const reception = await mockPo01Api.crearRecepcion({
      ...baseInput,
      humedadPct: 16,
    })
    expect(reception.estado).toBe('RECHAZADA')
    expect(reception.entradaKardexKg).toBeNull()
    expect((await mockPo01Api.getStockMaiz()).stockKg).toBe(8500)
  })

  it('solo el jefe acepta una recepción pendiente con motivo', async () => {
    await loginAs('operario_demo')
    const pending = await mockPo01Api.crearRecepcion({
      ...baseInput,
      humedadPct: 15,
    })
    await loginAs('jefe_demo')
    const accepted = await mockPo01Api.decidirRecepcion(pending.idRecepcion, {
      decision: 'ACEPTAR',
      motivo: 'Falta de maíz en planta',
    })
    expect(accepted.estado).toBe('ACEPTADA')
    expect(accepted.autorizadoPor).toBe('jefe_demo')
    expect((await mockPo01Api.getStockMaiz()).stockKg).toBe(28350)
  })

  it('exige motivo y no permite decidir dos veces', async () => {
    await loginAs('operario_demo')
    const pending = await mockPo01Api.crearRecepcion({
      ...baseInput,
      humedadPct: 14.8,
    })
    await loginAs('jefe_demo')
    await expect(
      mockPo01Api.decidirRecepcion(pending.idRecepcion, {
        decision: 'ACEPTAR',
        motivo: '   ',
      }),
    ).rejects.toThrow('motivo')
    await mockPo01Api.decidirRecepcion(pending.idRecepcion, {
      decision: 'RECHAZAR',
      motivo: 'Humedad fuera de rango',
    })
    await expect(
      mockPo01Api.decidirRecepcion(pending.idRecepcion, {
        decision: 'ACEPTAR',
        motivo: 'Segundo intento',
      }),
    ).rejects.toThrow('atendida')
  })

  it('el operario no puede decidir ni exportar', async () => {
    await loginAs('operario_demo')
    const pending = await mockPo01Api.crearRecepcion({
      ...baseInput,
      humedadPct: 14.8,
    })
    await expect(
      mockPo01Api.decidirRecepcion(pending.idRecepcion, {
        decision: 'ACEPTAR',
        motivo: 'Intento del operario',
      }),
    ).rejects.toThrow('permiso')
    expect(canExportKardex('OPERARIO')).toBe(false)
  })

  it('el administrador consulta pero no registra recepciones', async () => {
    await loginAs('admin_demo')
    expect(canRegisterReception('ADMINISTRADOR')).toBe(false)
    await expect(mockPo01Api.crearRecepcion(baseInput)).rejects.toThrow('permiso')
    expect(canExportKardex('ADMINISTRADOR')).toBe(true)
  })

  it('valida peso neto y merma', async () => {
    await loginAs('operario_demo')
    await expect(
      mockPo01Api.crearRecepcion({ ...baseInput, taraKg: 32500 }),
    ).rejects.toThrow('tara')
    await expect(
      mockPo01Api.crearRecepcion({ ...baseInput, mermaLimpiezaKg: 20000 }),
    ).rejects.toThrow('merma')
  })

  it('redirige por rol a recepción o kardex', () => {
    expect(homeForRole('OPERARIO')).toBe('/recepcion')
    expect(homeForRole('JEFE_PLANTA')).toBe('/recepcion')
    expect(homeForRole('CONTADORA')).toBe('/kardex')
    expect(homeForRole('ADMINISTRADOR')).toBe('/kardex')
  })
})
