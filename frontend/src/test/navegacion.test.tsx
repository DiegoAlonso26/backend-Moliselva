import { act } from 'react'
import { createRoot, type Root } from 'react-dom/client'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import App from '../App'
import { po01Api } from '../po01/api'

vi.mock('../po01/api', () => ({
  po01Api: {
    login: vi.fn(),
    getProveedores: vi.fn(),
    getRecepciones: vi.fn(),
    getKardex: vi.fn(),
    getStockMaiz: vi.fn(),
  },
}))

let host: HTMLDivElement
let root: Root

beforeEach(() => {
  Object.assign(globalThis, { IS_REACT_ACT_ENVIRONMENT: true })
  vi.clearAllMocks()
  window.sessionStorage.clear()
  window.history.replaceState(null, '', '/#/')
  vi.mocked(po01Api.getProveedores).mockResolvedValue([])
  vi.mocked(po01Api.getRecepciones).mockResolvedValue([])
  vi.mocked(po01Api.getKardex).mockResolvedValue([])
  vi.mocked(po01Api.getStockMaiz).mockResolvedValue({ stockKg: 0 })
  host = document.createElement('div')
  document.body.append(host)
  root = createRoot(host)
})

afterEach(async () => {
  await act(async () => root.unmount())
  host.remove()
  window.sessionStorage.clear()
  Object.assign(globalThis, { IS_REACT_ACT_ENVIRONMENT: false })
})

function guardarSesion(rol: 'JEFE_PLANTA' | 'CONTADORA') {
  window.sessionStorage.setItem('moliselva_session', JSON.stringify({
    token: 'token-ficticio-solo-de-prueba',
    usuario: { id: 7, nombres: 'Usuario de prueba', usuario: 'usuario_prueba', rol },
  }))
}

async function abrirApp() {
  await act(async () => root.render(<App />))
}

describe('Navegación de la aplicación con React Router actualizado', () => {
  it('redirige al acceso al abrir una ruta protegida sin sesión', async () => {
    window.history.replaceState(null, '', '/#/recepcion')

    await abrirApp()

    expect(window.location.hash).toBe('#/login')
    expect(host.querySelector('form')).not.toBeNull()
    expect(po01Api.getRecepciones).not.toHaveBeenCalled()
  })

  it('permite al jefe pasar de recepción a kardex y cerrar sesión', async () => {
    guardarSesion('JEFE_PLANTA')
    await abrirApp()
    expect(window.location.hash).toBe('#/recepcion')
    expect(host.querySelector('h1')?.textContent).toBe('Recepción de maíz')
    expect(po01Api.getRecepciones).toHaveBeenCalled()

    const enlace = host.querySelector<HTMLAnchorElement>('a[href="#/kardex"]')
    expect(enlace).not.toBeNull()
    await act(async () => {
      enlace!.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, button: 0 }))
    })
    expect(window.location.hash).toBe('#/kardex')
    expect(host.querySelector('h1')?.textContent).toBe('Kardex de maíz')
    expect(po01Api.getKardex).toHaveBeenCalled()

    const cerrar = host.querySelector<HTMLButtonElement>('button[aria-label="Cerrar sesión"]')
    expect(cerrar).not.toBeNull()
    await act(async () => cerrar!.click())
    expect(window.sessionStorage.getItem('moliselva_session')).toBeNull()
    expect(window.location.hash).toBe('#/login')
  })

  it('abre el kardex como página inicial de la contadora', async () => {
    guardarSesion('CONTADORA')

    await abrirApp()

    expect(window.location.hash).toBe('#/kardex')
    expect(host.querySelector('h1')?.textContent).toBe('Kardex de maíz')
    expect(po01Api.getStockMaiz).toHaveBeenCalled()
    expect(po01Api.getRecepciones).not.toHaveBeenCalled()
  })
})
