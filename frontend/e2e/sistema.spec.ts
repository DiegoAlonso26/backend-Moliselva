import { test, expect, request, type APIRequestContext, type Page, type Locator } from '@playwright/test'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { randomBytes } from 'node:crypto'

// Datos y cantidades matemáticas de prueba; no representan fórmulas del negocio.
type Row = Record<string, string | number | boolean | null>
const suffix = Date.now().toString(36)
const name = (kind: string) => `E2E ${kind} ${suffix}`
const credentials: Record<string, { username: string; password: string; token: string; id: number }> = {}
let api: APIRequestContext
let adminToken = ''
let provider = 0, input = 0, product = 0, client = 0, formula = 0, batch = 0, order = 0, dispatch = 0

async function call<T = Row>(path: string, method = 'GET', data?: unknown, role = 'JEFE_PLANTA'): Promise<T> {
  const result = await api.fetch(`/api${path}`, { method, data, headers: { Authorization: `Bearer ${credentials[role]?.token || adminToken}` } })
  if (!result.ok()) throw new Error(`Preparación/verificación HTTP ${method} ${path}: ${result.status()}`)
  return result.status() === 204 ? undefined as T : await result.json() as T
}
async function findId(path: string, field: string, value: string, id: string) {
  const rows = await call<Row[]>(path)
  const row = rows.find(r => r[field] === value)
  if (!row) throw new Error(`No se encontró el registro de prueba en ${path}`)
  return Number(row[id])
}
async function login(page: Page, role = 'JEFE_PLANTA') {
  await page.goto('/#/login')
  await page.locator('input[autocomplete="username"]').fill(credentials[role].username)
  await page.locator('input[autocomplete="current-password"]').fill(credentials[role].password)
  await page.getByRole('button', { name: /Ingresar|Iniciar sesión|Entrar/ }).click()
  await expect(page.getByRole('heading', { name: 'Inicio', exact: true, level: 1 })).toBeVisible()
}
async function go(page: Page, path: string, title: string) {
  await page.goto(`/#${path}`)
  await expect(page.getByRole('heading', { name: title, exact: true, level: 1 }).first()).toBeVisible()
  await expect(page.getByText('Cargando registros…')).toHaveCount(0)
}
async function fields(page: Page, values: Record<string, string>) {
  const dialog = page.getByRole('dialog')
  for (const [key, value] of Object.entries(values)) {
    const control = dialog.locator(`[name="${key}"]`)
    await expect(control).toBeEnabled()
    if (await control.evaluate(e => e.tagName === 'SELECT')) await control.selectOption(value)
    else await control.fill(value)
  }
}
async function save(page: Page, label = 'Guardar') {
  await page.getByRole('dialog').getByRole('button', { name: label, exact: true }).click()
  await expect(page.getByRole('dialog')).toBeHidden()
  await expect(page.getByText('Cargando registros…')).toHaveCount(0)
}
async function create(page: Page, singular: string, values: Record<string, string>, scope?: Locator) {
  await (scope || page).getByRole('button', { name: `Crear ${singular}`, exact: true }).click()
  await fields(page, values)
  await save(page)
}
function row(page: Page, id: number, scope?: Locator) {
  return (scope || page.locator('.resource-panel').first()).locator('tbody tr').filter({ has: page.locator('td:first-child', { hasText: new RegExp(`^${id}$`) }) })
}
async function action(page: Page, id: number, label: string, values: Record<string, string> = {}) {
  await row(page, id).getByRole('button', { name: label, exact: true }).click()
  await fields(page, values)
  await save(page, label)
}
async function remove(page: Page, id: number, label = 'Desactivar') {
  await row(page, id).getByRole('button', { name: label, exact: true }).click()
  await save(page, 'Confirmar')
  await expect(row(page, id)).toHaveCount(0)
}

test.describe.configure({ mode: 'serial' })
test.beforeAll(async () => {
  api = await request.newContext({ baseURL: 'http://localhost:8080' })
  const envPath = process.env.MOLISELVA_ENV_FILE || resolve('../.env')
  const demoPassword = process.env.DEMO_PASSWORD || readFileSync(envPath, 'utf8').split(/\r?\n/).find(line => line.startsWith('DEMO_PASSWORD='))?.slice(14)
  if (!demoPassword) throw new Error('Se requiere DEMO_PASSWORD local para preparar cuentas temporales.')
  const session = await api.post('/api/auth/login', { data: { usuario: 'admin_demo', password: demoPassword } })
  if (!session.ok()) throw new Error('No se pudo preparar la prueba con el administrador local.')
  adminToken = (await session.json()).token
  for (const role of ['ADMINISTRADOR', 'JEFE_PLANTA', 'OPERARIO', 'CONTADORA']) {
    const password = randomBytes(24).toString('hex')
    const username = `e2e_${role.toLowerCase()}_${suffix}`
    const account = await call('/usuarios', 'POST', { nombres: name(role), usuario: username, password, rol: role }, 'ADMINISTRADOR')
    const response = await api.post('/api/auth/login', { data: { usuario: username, password } })
    if (!response.ok()) throw new Error('No se pudo iniciar la cuenta temporal de prueba.')
    credentials[role] = { username, password, id: Number(account.id), token: (await response.json()).token }
  }
})
test.afterAll(async () => {
  if (api) {
    for (const account of Object.values(credentials)) await api.delete(`/api/usuarios/${account.id}`, { headers: { Authorization: `Bearer ${adminToken}` } })
    await api.dispose()
  }
})

test('catálogos: crear, editar, recargar, desactivar y proteger el maíz', async ({ page }) => {
  await login(page)
  await go(page, '/proveedores', 'Proveedores')
  await create(page, 'proveedor', { razonSocial: name('proveedor'), documento: `PRUEBA-${suffix}`, telefono: '000000000' })
  provider = await findId('/proveedores', 'razonSocial', name('proveedor'), 'idProveedor')
  await row(page, provider).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { telefono: '111111111' }); await save(page)
  await page.reload(); await page.getByRole('textbox', { name: 'Buscar en proveedores' }).fill('#' + provider); await expect(row(page, provider)).toContainText('111111111')
  await create(page, 'proveedor', { razonSocial: name('proveedor desactivar') })
  await remove(page, await findId('/proveedores', 'razonSocial', name('proveedor desactivar'), 'idProveedor'))
  await page.getByRole('textbox', { name: 'Buscar en proveedores' }).fill(name('proveedor'))
  await page.screenshot({ path: '../target/frontend-proveedores-desktop.png' })
  await go(page, '/insumos', 'Insumos')
  await create(page, 'insumo', { nombre: name('insumo'), unidadMedida: 'kg' })
  input = await findId('/insumos', 'nombre', name('insumo'), 'idInsumo')
  await row(page, input).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { unidadMedida: 'kg' }); await save(page)
  await create(page, 'insumo', { nombre: name('insumo desactivar'), unidadMedida: 'kg' })
  await remove(page, await findId('/insumos', 'nombre', name('insumo desactivar'), 'idInsumo'))
  const maize = (await call<Row[]>('/insumos')).find(r => r.nombre === 'Maíz')!
  await page.getByRole('textbox', { name: 'Buscar en insumos' }).fill('#' + maize.idInsumo)
  await expect(row(page, Number(maize.idInsumo)).getByRole('button', { name: 'Editar', exact: true })).toHaveCount(0)
  await go(page, '/clientes', 'Clientes')
  await create(page, 'cliente', { tipoCliente: 'OTRO', nombreRazonSocial: name('cliente'), zona: 'Zona de prueba' })
  client = await findId('/clientes', 'nombreRazonSocial', name('cliente'), 'idCliente')
  await row(page, client).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { zona: 'Zona revisada' }); await save(page)
  await create(page, 'cliente', { tipoCliente: 'OTRO', nombreRazonSocial: name('cliente desactivar') })
  await remove(page, await findId('/clientes', 'nombreRazonSocial', name('cliente desactivar'), 'idCliente'))
  await go(page, '/productos', 'Productos')
  await create(page, 'producto', { nombre: name('producto'), lineaComercial: 'OTRA' })
  product = await findId('/productos', 'nombre', name('producto'), 'idProducto')
  await row(page, product).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { fase: 'Fase de prueba' }); await save(page)
  await create(page, 'producto', { nombre: name('producto desactivar') })
  await remove(page, await findId('/productos', 'nombre', name('producto desactivar'), 'idProducto'))
})

test('recepción: corrección, eliminación, decisión, kardex y descarga auditada', async ({ page }) => {
  await login(page)
  await go(page, '/recepcion', 'Recepción de maíz')
  const values = { idProveedor: String(provider), pesoBrutoKg: '1000', taraKg: '100', pesoSacosKg: '10', humedadPct: '14.51', mermaLimpiezaKg: '10' }
  await create(page, 'recepción', values)
  const newest = async () => Math.max(...(await call<Row[]>('/recepciones')).filter(r => r.idProveedor === provider).map(r => Number(r.idRecepcion)))
  let id = await newest()
  await row(page, id).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { humedadPct: '14.50' }); await save(page)
  await expect(row(page, id)).toContainText('ACEPTADA')
  await expect(row(page, id).getByRole('button', { name: 'Eliminar', exact: true })).toHaveCount(0)
  await create(page, 'recepción', values); id = await newest()
  await action(page, id, 'Aceptar recepción', { motivo: 'Decisión automatizada de prueba' })
  await expect(row(page, id)).toContainText('ACEPTADA')
  await create(page, 'recepción', values); id = await newest()
  await action(page, id, 'Rechazar recepción', { motivo: 'Rechazo automatizado de prueba' })
  await expect(row(page, id)).toContainText('RECHAZADA')
  await create(page, 'recepción', values); id = await newest(); await remove(page, id, 'Eliminar')
  await go(page, '/kardex', 'Kardex de maíz')
  const download = page.waitForEvent('download')
  await page.getByRole('button', { name: 'Exportar CSV', exact: true }).click()
  expect((await download).suggestedFilename()).toMatch(/^kardex-maiz-.*\.csv$/)
  const events = await call<Row[]>('/bitacora')
  expect(events.some(r => r.accion === 'EXPORTAR_KARDEX_MAIZ' && r.usuario === credentials.JEFE_PLANTA.username)).toBe(true)
})

test('producción: fórmula, ingredientes, lote e inventario real', async ({ page }) => {
  await login(page)
  await go(page, '/movimientos', 'Movimientos y ajustes')
  await page.getByRole('button', { name: 'Registrar ajuste', exact: true }).click()
  await fields(page, { tipoMovimiento: 'AJUSTE_ENTRADA', idInsumo: String(input), cantidad: '600', motivo: 'Existencia abstracta de prueba' }); await save(page)
  await go(page, '/formulas', 'Fórmulas')
  await create(page, 'fórmula', { idProducto: String(product), version: suffix, vigente: 'true' })
  formula = Number((await call<Row[]>('/formulas')).find(r => r.idProducto === product)!.idFormula)
  const ingredients = page.locator('.resource-embedded')
  await create(page, 'ingrediente', { idInsumo: String(input), proporcionConfidencial: '100' }, ingredients)
  await row(page, input, ingredients).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { proporcionConfidencial: '100' }); await save(page)
  await row(page, input, ingredients).getByRole('button', { name: 'Eliminar', exact: true }).click(); await save(page, 'Confirmar')
  await create(page, 'ingrediente', { idInsumo: String(input), proporcionConfidencial: '100' }, ingredients)
  await action(page, formula, 'Retirar vigencia'); await action(page, formula, 'Activar vigencia')
  await go(page, '/lotes', 'Producción por lote')
  await create(page, 'lote', { idProducto: String(product), idFormula: String(formula), tamanoKg: '500' })
  batch = Number((await call<Row[]>('/lotes')).find(r => r.idProducto === product)!.idLote)
  const items = page.locator('.resource-embedded')
  await create(page, 'insumo del lote', { idInsumo: String(input), cantidadCalculadaKg: '500', cantidadRealKg: '499' }, items)
  await row(page, input, items).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { cantidadRealKg: '500' }); await save(page)
  await action(page, batch, 'Iniciar lote'); await action(page, batch, 'Finalizar lote')
  await expect(row(page, batch)).toContainText('TERMINADO')
  expect((await call(`/inventario/stock/PRODUCTO/${product}`)).stockActual).toBe(500)
  expect((await call(`/inventario/stock/INSUMO/${input}`)).stockActual).toBe(100)
  await go(page, '/insumos', 'Insumos')
  await page.getByRole('textbox', { name: 'Buscar en insumos' }).fill('#' + input)
  await row(page, input).getByRole('button', { name: 'Desactivar', exact: true }).click()
  await page.getByRole('dialog').getByRole('button', { name: 'Confirmar', exact: true }).click()
  await expect(page.getByRole('dialog').getByRole('alert')).toContainText('stock')
  await page.getByRole('dialog').getByRole('button', { name: 'Cancelar', exact: true }).click()
})

test('ventas: pedido y despacho completos con salida de inventario', async ({ page }) => {
  await login(page, 'CONTADORA')
  await go(page, '/pedidos', 'Pedidos')
  await create(page, 'pedido', { idCliente: String(client), observacion: name('pedido') })
  order = Number((await call<Row[]>('/pedidos', 'GET', undefined, 'CONTADORA')).find(r => r.observacion === name('pedido'))!.idPedido)
  const items = page.locator('.resource-embedded')
  await create(page, 'producto del pedido', { idProducto: String(product), cantidadSacos: '2', cantidadKg: '80' }, items)
  await row(page, product, items).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { cantidadKg: '80' }); await save(page)
  await action(page, order, 'Preparar pedido')
  await go(page, '/despachos', 'Despachos')
  await create(page, 'despacho', { idPedido: String(order), tipoEntrega: 'REPARTO', destino: 'Destino de prueba' })
  dispatch = Number((await call<Row[]>('/despachos', 'GET', undefined, 'CONTADORA')).find(r => r.idPedido === order)!.idDespacho)
  await create(page, 'producto del despacho', { idProducto: String(product), cantidadSacos: '2', cantidadKg: '80' }, page.locator('.resource-embedded'))
  await action(page, dispatch, 'Confirmar despacho')
  await expect(row(page, dispatch)).toContainText('CONFIRMADO')
  expect((await call(`/inventario/stock/PRODUCTO/${product}`)).stockActual).toBe(420)
  expect((await call(`/pedidos/${order}`, 'GET', undefined, 'CONTADORA')).estado).toBe('CERRADO')
})

test('operario: resumen de lotes y denegación real de composición y administración', async ({ page }) => {
  await login(page, 'OPERARIO')
  const requests: string[] = []
  page.on('request', r => requests.push(r.url()))
  await go(page, '/lotes', 'Producción por lote')
  await page.getByRole('textbox', { name: 'Buscar en producción por lote' }).fill('#' + batch)
  await row(page, batch).getByRole('button', { name: 'Ver detalle', exact: true }).click()
  await expect(page.locator('.resource-detail')).toBeVisible()
  await expect(page.locator('.resource-embedded')).toHaveCount(0)
  expect(requests.some(r => /\/formulas|\/lotes\/\d+\/insumos/.test(r))).toBe(false)
  for (const route of [`/api/lotes/${batch}/insumos`, `/api/lotes/${batch}/insumos/${input}`, '/api/formulas']) {
    const result = await api.get(route, { headers: { Authorization: `Bearer ${credentials.OPERARIO.token}` } })
    expect(result.status()).toBe(403)
  }
  await page.goto('/#/usuarios')
  await expect(page.getByRole('heading', { name: 'Sin permiso para esta sección' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Fórmulas', exact: true })).toHaveCount(0)
})

test('administración: usuarios, roles, bitácora y navegación móvil', async ({ page }) => {
  await login(page, 'ADMINISTRADOR')
  await go(page, '/usuarios', 'Usuarios')
  const username = `e2e_cuenta_${suffix}`
  const password = randomBytes(24).toString('hex')
  await create(page, 'usuario', { nombres: name('cuenta'), usuario: username, password, rol: 'OPERARIO' })
  const account = (await call<Row[]>('/usuarios', 'GET', undefined, 'ADMINISTRADOR')).find(r => r.usuario === username)!
  await row(page, Number(account.id)).getByRole('button', { name: 'Editar', exact: true }).click()
  await fields(page, { nombres: name('cuenta editada'), rol: 'CONTADORA' }); await save(page)
  const stillValid = await api.post('/api/auth/login', { data: { usuario: username, password } })
  expect(stillValid.ok()).toBe(true)
  expect((await stillValid.json()).usuario.rol).toBe('CONTADORA')
  await row(page, Number(account.id)).getByRole('button', { name: 'Desactivar', exact: true }).click(); await save(page, 'Confirmar')
  await expect(row(page, Number(account.id))).toContainText('Inactivo')
  const inactive = await api.post('/api/auth/login', { data: { usuario: username, password } }); expect(inactive.status()).toBe(401)
  await go(page, '/roles', 'Roles')
  await expect(page.locator('.resource-table tbody tr')).toHaveCount(4)
  await expect(page.getByRole('button', { name: 'Crear rol', exact: true })).toHaveCount(0)
  await go(page, '/bitacora', 'Bitácora')
  await expect(page.getByText('Consulta limitada a los últimos 200', { exact: false })).toBeVisible()
  await page.setViewportSize({ width: 390, height: 844 })
  await page.getByRole('button', { name: 'Abrir menú', exact: true }).click()
  await page.getByRole('link', { name: 'Proveedores', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Proveedores', exact: true })).toBeVisible()
  await page.getByRole('textbox', { name: 'Buscar en proveedores' }).fill(name('proveedor'))
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({ path: '../target/frontend-proveedores-mobile.png' })
})
