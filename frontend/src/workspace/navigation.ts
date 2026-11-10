import { ALL, MANAGERS } from './model'
import { providers, inputs, clients, products } from './catalogs'
import { receptions, formulas, batches } from './production'
import { orders, dispatches } from './sales'
import { users, roles, audit, stock, movements } from './administration'

export const resources = [
  { to: '/recepcion', group: 'Operaciones', config: receptions },
  { to: '/lotes', group: 'Operaciones', config: batches },
  { to: '/pedidos', group: 'Operaciones', config: orders },
  { to: '/despachos', group: 'Operaciones', config: dispatches },
  { to: '/inventario', group: 'Inventario', config: stock },
  { to: '/movimientos', group: 'Inventario', config: movements },
  { to: '/proveedores', group: 'Catálogos', config: providers },
  { to: '/insumos', group: 'Catálogos', config: inputs },
  { to: '/clientes', group: 'Catálogos', config: clients },
  { to: '/productos', group: 'Catálogos', config: products },
  { to: '/formulas', group: 'Catálogos', config: formulas },
  { to: '/usuarios', group: 'Administración', config: users },
  { to: '/roles', group: 'Administración', config: roles },
  { to: '/bitacora', group: 'Administración', config: audit },
]
export const navigation = [
  { to: '/inicio', group: 'Inicio', label: 'Inicio', read: ALL },
  ...resources.map(r => ({ to: r.to, group: r.group, label: r.config.title, read: r.config.read })),
  { to: '/kardex', group: 'Inventario', label: 'Kardex de maíz', read: ALL },
]
export const canManageCatalogs = (rol: string) => MANAGERS.some(r => r === rol)
