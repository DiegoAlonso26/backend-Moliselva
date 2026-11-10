import { ALL, MANAGERS, SALES, nameField, optional, type Resource } from './model'

export const providers: Resource = {
  title: 'Proveedores', singular: 'proveedor', path: '/proveedores', id: 'idProveedor',
  description: 'Proveedores activos disponibles para registrar recepciones. La desactivación conserva el historial.',
  read: ALL, write: MANAGERS, deactivate: true,
  fields: [nameField('razonSocial', 'Razón social', 160), optional('documento', 'Documento', 30), optional('telefono', 'Teléfono', 30)],
  columns: [{ key: 'idProveedor', label: 'Código' }, { key: 'razonSocial', label: 'Razón social' }, { key: 'documento', label: 'Documento' }, { key: 'telefono', label: 'Teléfono' }],
}
export const inputs: Resource = {
  title: 'Insumos', singular: 'insumo', path: '/insumos', id: 'idInsumo',
  description: 'Catálogo de insumos activos. Su existencia se consulta en Inventario.',
  warning: 'El insumo Maíz sostiene el kardex y está protegido. Los insumos con stock o referencias activas no se pueden desactivar.',
  read: ALL, write: MANAGERS, deactivate: true,
  fields: [nameField('nombre', 'Nombre', 120), { ...optional('unidadMedida', 'Unidad de medida', 20), default: 'kg' }],
  columns: [{ key: 'idInsumo', label: 'Código' }, { key: 'nombre', label: 'Nombre' }, { key: 'unidadMedida', label: 'Unidad de medida' }],
  editable: row => row.nombre !== 'Maíz',
}
export const clients: Resource = {
  title: 'Clientes', singular: 'cliente', path: '/clientes', id: 'idCliente', read: SALES, write: MANAGERS, deactivate: true,
  description: 'Clientes activos para pedidos y despachos.',
  fields: [{ key: 'tipoCliente', label: 'Tipo de cliente', required: true, type: 'select', options: ['REVENDEDOR', 'CRIADOR_TECNIFICADO', 'OTRO'] }, nameField('nombreRazonSocial', 'Nombre o razón social', 160), optional('documento', 'Documento', 30), optional('telefono', 'Teléfono', 30), optional('zona', 'Zona', 120)],
  columns: [{ key: 'idCliente', label: 'Código' }, { key: 'nombreRazonSocial', label: 'Nombre o razón social' }, { key: 'tipoCliente', label: 'Tipo' }, { key: 'documento', label: 'Documento' }, { key: 'telefono', label: 'Teléfono' }, { key: 'zona', label: 'Zona' }],
}
export const products: Resource = {
  title: 'Productos', singular: 'producto', path: '/productos', id: 'idProducto', read: ALL, write: MANAGERS, deactivate: true,
  description: 'Productos activos de la planta. No se desactivan productos con stock o referencias activas.',
  fields: [nameField('nombre', 'Nombre', 160), optional('especie', 'Especie', 60), optional('fase', 'Fase', 60), { key: 'lineaComercial', label: 'Línea comercial', type: 'select', options: ['GJA', 'MCDO', 'OTRA'] }, { ...optional('presentacion', 'Presentación', 80), default: 'Saco de 40 kg' }],
  columns: [{ key: 'idProducto', label: 'Código' }, { key: 'nombre', label: 'Nombre' }, { key: 'especie', label: 'Especie' }, { key: 'fase', label: 'Fase' }, { key: 'lineaComercial', label: 'Línea' }, { key: 'presentacion', label: 'Presentación' }],
}
