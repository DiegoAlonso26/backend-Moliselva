import { act } from 'react'
import { createRoot, type Root } from 'react-dom/client'
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import App from '../App'
import type { Rol } from '../po01/types'
let host: HTMLDivElement, root: Root, rol: Rol, conflict: boolean
let proveedores: Array<{ idProveedor: number; razonSocial: string }>, calls: string[]
beforeEach(() => {
  Object.assign(globalThis, { IS_REACT_ACT_ENVIRONMENT: true })
  rol = 'JEFE_PLANTA'; conflict = false; calls = []
  proveedores = [{ idProveedor: 7, razonSocial: 'Proveedor de prueba' }]
  sessionStorage.clear(); window.history.replaceState(null, '', '/#/')
  vi.stubGlobal('fetch', vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const path = new URL(String(input), 'http://localhost').pathname.replace(/^\/api/, '')
    const method = init?.method || 'GET'; calls.push(method + ' ' + path)
    if (path === '/auth/me') return Response.json({ id: 7, nombres: 'Usuario de prueba', usuario: 'usuario_prueba', rol, activo: true })
    if (path === '/inventario/maiz/stock') return Response.json({ stockKg: 0 })
    if (path === '/inventario/maiz/kardex' || path === '/recepciones') return Response.json([])
    if (path === '/proveedores' && method === 'GET') return Response.json(proveedores)
    if (path === '/proveedores' && method === 'POST') {
      if (conflict) return Response.json({ codigo: 'CONFLICTO_DATOS', mensaje: 'Documento duplicado de prueba.' }, { status: 409 })
      const row = { ...JSON.parse(String(init?.body)), idProveedor: 8 }; proveedores.push(row)
      return Response.json(row, { status: 201 })
    }
    if (path === '/proveedores/7' && method === 'DELETE') { proveedores = []; return new Response(null, { status: 204 }) }
    throw new Error('Petición inesperada: ' + method + ' ' + path)
  }))
  host = document.createElement('div'); document.body.append(host); root = createRoot(host)
})
afterEach(async () => {
  await act(async () => root.unmount()); host.remove(); sessionStorage.clear(); vi.unstubAllGlobals()
  Object.assign(globalThis, { IS_REACT_ACT_ENVIRONMENT: false })
})
function session(role: Rol = 'JEFE_PLANTA') {
  rol = role
  sessionStorage.setItem('moliselva_session', JSON.stringify({ token: 'token-ficticio', usuario: { id: 7, nombres: 'Usuario de prueba', usuario: 'usuario_prueba', rol } }))
}
async function open(path = '/') {
  window.history.replaceState(null, '', '/#' + path); await act(async () => root.render(<App />))
}
async function click(label: string) {
  const button = [...host.querySelectorAll('button')].find(b => b.textContent?.trim() === label)
  expect(button).toBeTruthy(); await act(async () => button!.click())
}
async function fill(name: string, value: string) {
  const input = host.querySelector<HTMLInputElement>(`input[name="${name}"]`)!
  await act(async () => { Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(input, value); input.dispatchEvent(new Event('input', { bubbles: true })) })
}
async function save() { await act(async () => host.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }))) }
it('redirige rutas protegidas sin sesión sin llamar a la API', async () => {
  await open('/recepcion'); expect(window.location.hash).toBe('#/login'); expect(calls).toHaveLength(0)
})
it('valida sesión, abre inicio, navega a kardex y cierra sesión', async () => {
  session(); await open(); expect(window.location.hash).toBe('#/inicio'); expect(calls).toContain('GET /auth/me')
  await act(async () => host.querySelector<HTMLAnchorElement>('a[href="#/kardex"]')!.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, button: 0 })))
  expect(host.querySelector('h1')?.textContent).toBe('Kardex de maíz')
  await act(async () => host.querySelector<HTMLButtonElement>('button[aria-label="Cerrar sesión"]')!.click())
  expect(sessionStorage.getItem('moliselva_session')).toBeNull(); expect(window.location.hash).toBe('#/login')
})
it('impide a contadora abrir usuarios sin consultar el endpoint', async () => {
  session('CONTADORA'); await open('/usuarios'); expect(window.location.hash).toBe('#/sin-acceso')
  expect(calls.some(c => c.includes('/usuarios'))).toBe(false); expect(host.querySelector('a[href="#/usuarios"]')).toBeNull()
})
it('el operario consulta proveedores sin acciones de escritura', async () => {
  session('OPERARIO'); await open('/proveedores'); expect(host.textContent).toContain('Proveedor de prueba')
  expect(host.textContent).not.toContain('Crear proveedor'); expect(host.textContent).not.toContain('Desactivar')
})
it('crea proveedor con el contrato HTTP y actualiza el listado', async () => {
  session(); await open('/proveedores'); await click('Crear proveedor'); await fill('razonSocial', 'Nuevo proveedor de prueba'); await save()
  expect(calls).toContain('POST /proveedores'); expect(proveedores.at(-1)?.razonSocial).toBe('Nuevo proveedor de prueba')
  expect(host.querySelector('[role="dialog"]')).toBeNull(); expect(host.textContent).toContain('Nuevo proveedor de prueba')
})
it('mantiene el formulario y el mensaje ante conflicto del servidor', async () => {
  session(); conflict = true; await open('/proveedores'); await click('Crear proveedor'); await fill('razonSocial', 'Conservar borrador'); await save()
  expect(host.querySelector('[role="dialog"]')).not.toBeNull()
  expect(host.querySelector<HTMLInputElement>('[name="razonSocial"]')?.value).toBe('Conservar borrador')
  expect(host.textContent).toContain('Documento duplicado de prueba.')
})
it('confirma desactivación y procesa respuesta 204', async () => {
  session(); await open('/proveedores'); await click('Desactivar'); await click('Confirmar')
  expect(calls).toContain('DELETE /proveedores/7'); expect(host.querySelector('[role="dialog"]')).toBeNull(); expect(proveedores).toHaveLength(0)
})
