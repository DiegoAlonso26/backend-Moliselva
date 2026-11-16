import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { ApiError, request, send } from '../shared/http'

beforeEach(() => {
  sessionStorage.setItem('moliselva_session', JSON.stringify({ token: 'token-ficticio' }))
})
afterEach(() => { vi.unstubAllGlobals(); sessionStorage.clear() })

it('envía el DTO y el token una sola vez sin duplicar /api', async () => {
  const fetch = vi.fn().mockResolvedValue(Response.json({ idProveedor: 7, razonSocial: 'Proveedor de prueba' }, { status: 201 }))
  vi.stubGlobal('fetch', fetch)
  await send('/proveedores', 'POST', { razonSocial: 'Proveedor de prueba' })
  expect(fetch).toHaveBeenCalledTimes(1)
  const [url, init] = fetch.mock.calls[0]
  expect(String(url).endsWith('/api/proveedores')).toBe(true)
  expect(String(url)).not.toContain('/api/api/')
  expect(init.headers.get('Authorization')).toBe('Bearer token-ficticio')
  expect(JSON.parse(init.body)).toEqual({ razonSocial: 'Proveedor de prueba' })
})

it('acepta un 204 de borrado sin intentar interpretar JSON', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 204 })))
  expect(await send('/proveedores/7', 'DELETE')).toBeUndefined()
})

it('conserva el mensaje y el código de un conflicto del backend', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(Response.json({ codigo: 'INSUMO_SISTEMA', mensaje: 'El maíz está protegido.' }, { status: 409 })))
  await expect(send('/insumos/1', 'DELETE')).rejects.toMatchObject({ status: 409, codigo: 'INSUMO_SISTEMA', message: 'El maíz está protegido.' })
})

it('notifica el vencimiento para limpiar la sesión protegida', async () => {
  const expired = vi.fn()
  window.addEventListener('moliselva:unauthorized', expired)
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(Response.json({ codigo: 'NO_AUTENTICADO', mensaje: 'Token ausente o inválido' }, { status: 401 })))
  await expect(request('/insumos')).rejects.toBeInstanceOf(ApiError)
  expect(expired).toHaveBeenCalledTimes(1)
  window.removeEventListener('moliselva:unauthorized', expired)
})
