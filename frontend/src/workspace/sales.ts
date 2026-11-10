import { SALES, nameField, numberField, optional, ref, selectRef, type Resource } from './model'
import { productRef } from './production'

const details = (path: string, type: 'pedido' | 'despacho', editable: boolean): Resource => ({
  title: 'Productos registrados', singular: `producto del ${type}`, path, id: 'idProducto', read: SALES, write: SALES,
  description: 'Cada producto se guarda por separado. Revise sacos y kilos antes de cambiar el estado.',
  noCreate: !editable, noEdit: !editable, noDelete: !editable,
  fields: [{ ...selectRef('idProducto', 'Producto', productRef), immutable: true }, numberField('cantidadSacos', 'Cantidad de sacos'), numberField('cantidadKg', 'Cantidad (kg)', 0.01, 0.01)],
  columns: [{ key: 'idProducto', label: 'Producto', reference: productRef }, { key: 'cantidadSacos', label: 'Sacos' }, { key: 'cantidadKg', label: 'Cantidad (kg)' }],
})
export const orders: Resource = {
  title: 'Pedidos', singular: 'pedido', path: '/pedidos', id: 'idPedido', read: SALES, write: SALES,
  description: 'Pedidos y sus productos. Guarde el encabezado y agregue los productos desde el detalle antes de prepararlo.',
  fields: [selectRef('idCliente', 'Cliente', ref('/clientes', 'idCliente', 'nombreRazonSocial')), { ...optional('observacion', 'Observación', 300), type: 'textarea' }],
  columns: [{ key: 'idPedido', label: 'Código' }, { key: 'idCliente', label: 'Cliente', reference: ref('/clientes', 'idCliente', 'nombreRazonSocial') }, { key: 'fechaHora', label: 'Fecha y hora' }, { key: 'observacion', label: 'Observación' }, { key: 'estado', label: 'Estado' }],
  editable: r => r.estado === 'PENDIENTE',
  actions: [{ label: 'Preparar pedido', method: 'PATCH', suffix: '/estado', body: { estado: 'EN_PREPARACION' }, available: r => r.estado === 'PENDIENTE', description: 'El pedido debe tener al menos un producto. Sus detalles dejarán de ser editables.' }, { label: 'Cancelar pedido', method: 'PATCH', suffix: '/estado', body: { estado: 'CANCELADO' }, available: r => ['PENDIENTE', 'EN_PREPARACION'].includes(String(r.estado)), description: 'Solo se puede cancelar si no tiene despachos asociados.' }],
  children: r => [details(`/pedidos/${r.idPedido}/detalles`, 'pedido', r.estado === 'PENDIENTE')],
}
export const dispatches: Resource = {
  title: 'Despachos', singular: 'despacho', path: '/despachos', id: 'idDespacho', read: SALES, write: SALES,
  description: 'Borradores de despacho y confirmación de salidas. Agregue los productos desde el detalle antes de confirmar.',
  fields: [selectRef('idPedido', 'Pedido', { ...ref('/pedidos', 'idPedido', 'idPedido'), filter: r => ['PENDIENTE', 'EN_PREPARACION'].includes(String(r.estado)) }),
    { key: 'tipoEntrega', label: 'Tipo de entrega', type: 'select', options: ['RECOJO_PLANTA', 'REPARTO'], required: true }, optional('destino', 'Destino', 160)],
  validate: v => v.tipoEntrega === 'REPARTO' && !v.destino ? 'El reparto requiere un destino.' : null,
  columns: [{ key: 'idDespacho', label: 'Código' }, { key: 'idPedido', label: 'Pedido' }, { key: 'fechaHora', label: 'Fecha y hora' }, { key: 'tipoEntrega', label: 'Entrega' }, { key: 'destino', label: 'Destino' }, { key: 'estado', label: 'Estado' }],
  editable: r => r.estado === 'BORRADOR',
  actions: [{ label: 'Confirmar despacho', method: 'POST', suffix: '/confirmar', available: r => r.estado === 'BORRADOR', description: 'Se comprobarán el pedido y el stock, y se registrarán las salidas. Un despacho confirmado conserva su historial y no se edita.' }],
  children: r => [details(`/despachos/${r.idDespacho}/detalles`, 'despacho', r.estado === 'BORRADOR')],
}
