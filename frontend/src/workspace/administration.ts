import { ALL, MANAGERS, SALES, nameField, numberField, optional, selectRef, type Resource } from './model'
import { inputRef, productRef } from './production'

export const users: Resource = {
  title: 'Usuarios', singular: 'usuario', path: '/usuarios', id: 'id', read: ['ADMINISTRADOR'], write: ['ADMINISTRADOR'], deactivate: true,
  description: 'Cuentas y permisos. La desactivación impide utilizar sus tokens. Los usuarios inactivos se conservan como historial.',
  fields: [nameField('nombres', 'Nombres', 120), nameField('usuario', 'Usuario', 60), { key: 'password', label: 'Contraseña', type: 'password', required: true, hint: 'Mínimo 12 caracteres.' }, { key: 'rol', label: 'Rol', type: 'select', options: ALL, required: true }],
  columns: [{ key: 'id', label: 'Código' }, { key: 'nombres', label: 'Nombres' }, { key: 'usuario', label: 'Usuario' }, { key: 'rol', label: 'Rol' }, { key: 'activo', label: 'Estado de cuenta' }],
  editable: r => Boolean(r.activo), deletable: (r, uid) => Boolean(r.activo) && r.id !== uid,
}
export const roles: Resource = {
  title: 'Roles', singular: 'rol', path: '/roles', id: 'idRol', read: ['ADMINISTRADOR'], fields: [],
  description: 'Los cuatro roles son fijos y determinan los permisos del sistema. Se asignan desde Usuarios.',
  columns: [{ key: 'idRol', label: 'Código' }, { key: 'nombre', label: 'Rol' }, { key: 'descripcion', label: 'Descripción' }],
}
export const audit: Resource = {
  title: 'Bitácora', singular: 'evento', path: '/bitacora', id: 'idBitacora', read: MANAGERS, fields: [], limit: 200,
  description: 'Registro de acciones con usuario y motivo. El historial se conserva sin ediciones ni eliminaciones.',
  map: r => ({ idBitacora: r.id_bitacora, fechaHora: r.fecha_hora, usuario: r.usuario, accion: r.accion, motivo: r.motivo }),
  columns: [{ key: 'idBitacora', label: 'Código' }, { key: 'fechaHora', label: 'Fecha y hora' }, { key: 'usuario', label: 'Usuario' }, { key: 'accion', label: 'Acción' }, { key: 'motivo', label: 'Motivo' }],
}
export const stock: Resource = {
  title: 'Existencias', singular: 'existencia', path: '/inventario/stock', id: 'idItem', read: ALL, fields: [],
  description: 'Stock actual calculado a partir de los movimientos. Las correcciones se registran con motivo en Movimientos y ajustes.',
  columns: [{ key: 'tipoItem', label: 'Tipo' }, { key: 'idItem', label: 'Código' }, { key: 'nombre', label: 'Nombre' }, { key: 'unidadMedida', label: 'Unidad' }, { key: 'stockActual', label: 'Stock actual' }],
  itemPath: r => `/inventario/stock/${r.tipoItem}/${r.idItem}`,
}
export const movements: Resource = {
  title: 'Movimientos y ajustes', singular: 'movimiento', path: '/inventario/movimientos', id: 'idMovimiento', read: ALL, write: SALES,
  description: 'Historial de insumos y productos. Los ajustes compensan diferencias sin modificar movimientos anteriores.',
  createLabel: 'Registrar ajuste', createPath: '/inventario/movimientos/ajustes', noEdit: true, noDelete: true, limit: 500,
  fields: [{ key: 'tipoMovimiento', label: 'Tipo de ajuste', type: 'select', required: true, options: ['AJUSTE_ENTRADA', 'AJUSTE_SALIDA'] },
    { ...selectRef('idInsumo', 'Insumo', inputRef), required: false, hint: 'Seleccione un insumo o un producto, nunca ambos.' },
    { ...selectRef('idProducto', 'Producto', productRef), required: false }, numberField('cantidad', 'Cantidad', 0.001, 0.001), nameField('motivo', 'Motivo del ajuste', 300)],
  validate: v => Boolean(v.idInsumo) === Boolean(v.idProducto) ? 'Seleccione exactamente un insumo o un producto.' : null,
  columns: [{ key: 'idMovimiento', label: 'Código' }, { key: 'fechaHora', label: 'Fecha y hora' }, { key: 'tipoMovimiento', label: 'Movimiento' }, { key: 'idInsumo', label: 'Insumo', reference: inputRef }, { key: 'idProducto', label: 'Producto', reference: productRef }, { key: 'cantidad', label: 'Cantidad' }, { key: 'unidadMedida', label: 'Unidad' }, { key: 'idRecepcion', label: 'Recepción' }, { key: 'idLote', label: 'Lote' }, { key: 'idDespacho', label: 'Despacho' }, { key: 'usuario', label: 'Usuario' }],
}
