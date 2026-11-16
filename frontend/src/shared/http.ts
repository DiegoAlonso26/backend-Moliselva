export const SESSION_KEY = 'moliselva_session'
const BASE_URL = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(public status: number, public codigo: string, message: string) {
    super(message)
  }
}

export async function response(path: string, options: RequestInit = {}, authenticated = true) {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')
  if (options.body) headers.set('Content-Type', 'application/json')
  if (authenticated) {
    try {
      const session = JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null')
      if (session?.token) headers.set('Authorization', `Bearer ${session.token}`)
    } catch { sessionStorage.removeItem(SESSION_KEY) }
  }
  const result = await fetch(`${BASE_URL}${path}`, { ...options, headers })
  if (!result.ok) {
    const payload = await result.json().catch(() => null)
    if (authenticated && result.status === 401) window.dispatchEvent(new Event('moliselva:unauthorized'))
    if (authenticated && result.status === 403 && path !== '/auth/me') window.dispatchEvent(new Event('moliselva:forbidden'))
    throw new ApiError(result.status, payload?.codigo || 'ERROR_HTTP', payload?.mensaje || `No se pudo completar la operación (${result.status}).`)
  }
  return result
}

export async function request<T>(path: string, options: RequestInit = {}, authenticated = true): Promise<T> {
  const result = await response(path, options, authenticated)
  if (result.status === 204) return undefined as T
  return result.json() as Promise<T>
}

export const send = <T>(path: string, method: string, data?: unknown) => request<T>(path, {
  method, ...(data === undefined ? {} : { body: JSON.stringify(data) }),
})

export async function download(path: string) {
  const result = await response(path)
  const disposition = result.headers.get('Content-Disposition') || ''
  const encoded = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
  const plain = disposition.match(/filename="?([^";]+)"?/i)?.[1]
  let filename = plain || 'kardex-maiz.csv'
  if (encoded) { try { filename = decodeURIComponent(encoded) } catch { /* Usar nombre simple. */ } }
  const url = URL.createObjectURL(await result.blob())
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename.replace(/[\\/]/g, '_')
  document.body.append(anchor)
  anchor.click()
  anchor.remove()
  window.setTimeout(() => URL.revokeObjectURL(url), 1000)
}
