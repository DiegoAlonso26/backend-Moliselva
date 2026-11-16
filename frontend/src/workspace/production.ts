import { ALL, CHIEF, nameField, numberField, ref, selectRef, type Resource, type Row } from './model'

export const inputRef = ref('/insumos', 'idInsumo', 'nombre')
export const productRef = ref('/productos', 'idProducto', 'nombre')
const formulaRef = { ...ref('/formulas', 'idFormula', 'version'), filter: (r: Row, v: Row) => Boolean(r.vigente) && String(r.idProducto) === String(v.idProducto) }
export const receptions: Resource = {
  title: 'Recepción de maíz', singular: 'recepción', path: '/recepciones', id: 'idRecepcion', read: ALL, write: ['JEFE_PLANTA', 'OPERARIO'], filterDate: true,
  description: 'Registre pesos, humedad y merma. El sistema calcula el peso neto y genera la entrada al kardex cuando corresponde.',
  warning: 'Hasta 14.5% se acepta; por encima de 14.5% y hasta 15% requiere decisión del jefe; más de 15% se rechaza. Una recepción aceptada o decidida no se puede corregir ni eliminar.',
  fields: [selectRef('idProveedor', 'Proveedor', ref('/proveedores', 'idProveedor', 'razonSocial')),
    numberField('pesoBrutoKg', 'Peso bruto (kg)', 0.01, 0.01), { ...numberField('taraKg', 'Tara (kg)', 0.01, 0), default: 0 },
    { ...numberField('pesoSacosKg', 'Peso de sacos (kg)', 0.01, 0), default: 0 },
    { ...numberField('humedadPct', 'Humedad (%)', 0.01, 0), max: 100 },
    { ...numberField('mermaLimpiezaKg', 'Merma de limpieza (kg)', 0.01, 0), default: 0 }],
  columns: [{ key: 'idRecepcion', label: 'Código' }, { key: 'fechaHora', label: 'Fecha y hora' }, { key: 'proveedor', label: 'Proveedor' }, { key: 'pesoBrutoKg', label: 'Bruto (kg)' }, { key: 'taraKg', label: 'Tara (kg)' }, { key: 'pesoSacosKg', label: 'Sacos (kg)' }, { key: 'pesoNetoKg', label: 'Neto (kg)' }, { key: 'humedadPct', label: 'Humedad (%)' }, { key: 'mermaLimpiezaKg', label: 'Merma (kg)' }, { key: 'entradaKardexKg', label: 'Entrada (kg)' }, { key: 'estado', label: 'Estado' }, { key: 'registradoPor', label: 'Registrado por' }, { key: 'autorizadoPor', label: 'Decidido por' }],
  editable: r => r.estado !== 'ACEPTADA' && !r.autorizadoPor,
  preview: v => {
    const neto = Number(v.pesoBrutoKg || 0) - Number(v.taraKg || 0) - Number(v.pesoSacosKg || 0)
    return `Neto estimado: ${neto.toFixed(2)} kg · Entrada si se acepta: ${(neto - Number(v.mermaLimpiezaKg || 0)).toFixed(3)} kg. El servidor confirmará los valores al guardar.`
  },
  validate: v => Number(v.pesoBrutoKg) - Number(v.taraKg) - Number(v.pesoSacosKg) <= Number(v.mermaLimpiezaKg) ? 'El peso neto debe ser positivo y la merma menor que el neto.' : null,
  actions: ['ACEPTAR', 'RECHAZAR'].map(decision => ({ label: decision === 'ACEPTAR' ? 'Aceptar recepción' : 'Rechazar recepción', method: 'PATCH' as const, suffix: '/decision', body: { decision },
    roles: CHIEF, available: r => r.estado === 'PENDIENTE', fields: [nameField('motivo', 'Motivo de la decisión', 300)], description: 'Solo el jefe de planta puede decidir. Al aceptar, la entrada al kardex se registra una sola vez.' })),
}
export const formulas: Resource = {
  title: 'Fórmulas', singular: 'fórmula', path: '/formulas', id: 'idFormula', read: CHIEF, write: CHIEF, limit: 500,
  description: 'Versiones e ingredientes de fórmulas. Información confidencial disponible únicamente para el jefe de planta.',
  warning: 'Una fórmula utilizada por un lote conserva su composición. El servidor bloqueará su edición o eliminación; puede cambiarse su vigencia.',
  fields: [selectRef('idProducto', 'Producto', productRef), nameField('version', 'Versión', 40), { key: 'vigente', label: 'Vigente', type: 'select', options: ['true', 'false'], default: 'true', required: true }],
  columns: [{ key: 'idFormula', label: 'Código' }, { key: 'idProducto', label: 'Producto', reference: productRef }, { key: 'version', label: 'Versión' }, { key: 'vigente', label: 'Vigente' }],
  payload: values => ({ ...values, vigente: values.vigente === 'true' }),
  actions: [{ label: 'Activar vigencia', method: 'PATCH', suffix: '/vigencia', body: { vigente: true }, available: r => !r.vigente, description: 'Activar esta versión para la planificación de lotes.' }, { label: 'Retirar vigencia', method: 'PATCH', suffix: '/vigencia', body: { vigente: false }, available: r => Boolean(r.vigente), description: 'La composición y los lotes históricos se conservarán.' }],
  children: row => [{ title: 'Ingredientes de la fórmula', singular: 'ingrediente', path: `/formulas/${row.idFormula}/detalles`, id: 'idInsumo', read: CHIEF, write: CHIEF,
    description: 'Agregue los insumos de esta versión y sus proporciones. Solo se permiten cambios antes de utilizarla en un lote.',
    fields: [{ ...selectRef('idInsumo', 'Insumo', inputRef), immutable: true }, { ...numberField('proporcionConfidencial', 'Proporción confidencial (%)', 0.0001, 0.0001), max: 100 }],
    columns: [{ key: 'idInsumo', label: 'Insumo', reference: inputRef }, { key: 'proporcionConfidencial', label: 'Proporción confidencial (%)' }],
  }],
}
export const batches: Resource = {
  title: 'Producción por lote', singular: 'lote', path: '/lotes', id: 'idLote', read: ['JEFE_PLANTA', 'OPERARIO'], write: CHIEF,
  description: 'Planifique, registre insumos, inicie y finalice la producción. El operario consulta únicamente el resumen de los lotes.',
  fields: [selectRef('idProducto', 'Producto', productRef), selectRef('idFormula', 'Fórmula vigente del producto', formulaRef), { key: 'tamanoKg', label: 'Tamaño del lote (kg)', type: 'select', options: ['500', '1000'], default: '500', required: true }],
  payload: v => ({ ...v, tamanoKg: Number(v.tamanoKg) }),
  columns: [{ key: 'idLote', label: 'Código' }, { key: 'idProducto', label: 'Producto', reference: productRef }, { key: 'fechaHora', label: 'Fecha y hora' }, { key: 'tamanoKg', label: 'Tamaño (kg)' }, { key: 'estado', label: 'Estado' }],
  editable: r => r.estado === 'PLANIFICADO',
  actions: [{ label: 'Iniciar lote', method: 'PATCH', suffix: '/estado', body: { estado: 'EN_PROCESO' }, available: r => r.estado === 'PLANIFICADO', description: 'El lote debe tener sus insumos registrados antes de iniciar.' },
    { label: 'Anular lote', method: 'PATCH', suffix: '/estado', body: { estado: 'ANULADO' }, available: r => ['PLANIFICADO', 'EN_PROCESO'].includes(String(r.estado)), description: 'El servidor verificará que el lote no tenga movimientos de inventario.' },
    { label: 'Finalizar lote', method: 'POST', suffix: '/finalizar', available: r => r.estado === 'EN_PROCESO', description: 'Se descontarán los insumos y se ingresará producto terminado en una transacción. Revise cantidades reales y stock antes de confirmar.' }],
  children: row => [{ title: 'Insumos del lote', singular: 'insumo del lote', path: `/lotes/${row.idLote}/insumos`, id: 'idInsumo', read: CHIEF, write: CHIEF,
    description: 'Registre todos los insumos de la fórmula antes de finalizar. Las cantidades reales pueden corregirse en proceso antes de los movimientos.',
    noCreate: row.estado !== 'PLANIFICADO', noEdit: !['PLANIFICADO', 'EN_PROCESO'].includes(String(row.estado)), noDelete: row.estado !== 'PLANIFICADO',
    fields: [{ ...selectRef('idInsumo', 'Insumo', inputRef), immutable: true }, numberField('cantidadCalculadaKg', 'Cantidad calculada (kg)', 0.001, 0.001), { ...numberField('cantidadRealKg', 'Cantidad real (kg)', 0.001, 0.001), required: false, hint: 'Si falta, al finalizar se utiliza la cantidad calculada.' }],
    columns: [{ key: 'idInsumo', label: 'Insumo', reference: inputRef }, { key: 'cantidadCalculadaKg', label: 'Calculada (kg)' }, { key: 'cantidadRealKg', label: 'Real (kg)' }],
  }],
}
