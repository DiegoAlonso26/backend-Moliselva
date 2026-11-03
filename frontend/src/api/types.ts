export type Rol = 'ADMINISTRADOR' | 'JEFE_PLANTA' | 'OPERARIO' | 'CONTADORA'

export interface Usuario {
  id_usuario: number
  nombres: string
  usuario: string
  rol: Rol
}

export interface Session {
  token: string
  usuario: Usuario
}

export interface Proveedor {
  id_proveedor: number
  razon_social: string
  documento?: string | null
  telefono?: string | null
}

export type TipoCliente = 'REVENDEDOR' | 'CRIADOR_TECNIFICADO' | 'OTRO'

export interface Cliente {
  id_cliente: number
  tipo_cliente: TipoCliente
  nombre_razon_social: string
  zona?: string | null
  telefono?: string | null
}

export interface Insumo {
  id_insumo: number
  nombre: string
  unidad_medida: string
  stock_kg: number
}

export interface Producto {
  id_producto: number
  nombre: string
  especie: string
  fase: string
  linea_comercial: string | null
  presentacion: string
  stock_kg: number
}

export interface FormulaResumen {
  id_formula: number
  id_producto: number
  producto_nombre: string
  version: string
  vigente: boolean
  confidencial: boolean
}

export interface FormulaMacro {
  id_insumo: number
  insumo: string
  cantidad_calculada_kg: number
  cantidad_real_kg: number | null
  proporcion_pct: number
}

export interface FormulaDetalle extends FormulaResumen {
  macros: FormulaMacro[]
  nota: string
}

export type EstadoRecepcion = 'PENDIENTE' | 'ACEPTADA' | 'RECHAZADA'

export interface Recepcion {
  id_recepcion: number
  id_proveedor: number
  proveedor_nombre: string
  id_insumo: number
  insumo_nombre: string
  fecha_hora: string
  peso_bruto_kg: number
  tara_kg: number
  peso_sacos_kg: number
  peso_neto_kg: number
  humedad_pct: number
  estado: EstadoRecepcion
  id_autoriza: number | null
  autoriza_nombre: string | null
}

export interface RecepcionInput {
  id_proveedor: number
  id_insumo: number
  peso_bruto_kg: number
  tara_kg: number
  peso_sacos_kg: number
  humedad_pct: number
  autorizar: boolean
}

export type EstadoLote = 'PLANIFICADO' | 'EN_PROCESO' | 'TERMINADO' | 'ANULADO'

export interface Lote {
  id_lote: number
  id_producto: number
  producto_nombre: string
  id_formula: number
  formula_version: string
  fecha_hora: string
  tamano_kg: number
  estado: EstadoLote
  id_responsable: number
  responsable_nombre: string
  macros: FormulaMacro[]
}

export interface LoteInput {
  id_producto: number
  id_formula: number
  tamano_kg: number
}

export interface LoteRealInput {
  macros: Array<{
    id_insumo: number
    cantidad_real_kg: number
  }>
}

export type TipoMovimiento =
  | 'ENTRADA'
  | 'SALIDA'
  | 'AJUSTE_ENTRADA'
  | 'AJUSTE_SALIDA'

export type TipoItem = 'INSUMO' | 'PRODUCTO'

export interface Movimiento {
  id_movimiento: number
  fecha_hora: string
  tipo_movimiento: TipoMovimiento
  tipo_item: TipoItem
  id_item: number
  item: string
  cantidad: number
  unidad_medida: string
  referencia: string | null
  id_usuario: number
  usuario_nombre: string
  saldo: number
}

export interface PedidoItem {
  id_producto: number
  producto_nombre: string
  cantidad_sacos: number
  cantidad_kg: number
}

export type EstadoPedido = 'PENDIENTE' | 'EN_PREPARACION' | 'ATENDIDO' | 'CANCELADO'

export interface Pedido {
  id_pedido: number
  id_cliente: number
  cliente_nombre: string
  fecha_hora: string
  estado: EstadoPedido
  observacion: string | null
  items: PedidoItem[]
}

export interface PedidoInput {
  id_cliente: number
  observacion?: string
  items: Array<{
    id_producto: number
    cantidad_sacos: number
  }>
}

export type TipoEntrega = 'RECOJO_PLANTA' | 'REPARTO'

export type EstadoDespacho = 'BORRADOR' | 'CONFIRMADO' | 'ANULADO'

export interface Despacho {
  id_despacho: number
  id_pedido: number
  cliente_nombre: string
  fecha_hora: string
  tipo_entrega: TipoEntrega
  destino: string | null
  estado: EstadoDespacho
  id_usuario: number
  usuario_nombre: string
  items: PedidoItem[]
}

export interface DespachoInput {
  id_pedido: number
  tipo_entrega: TipoEntrega
  destino?: string
}

export interface StockResumen {
  productos: Producto[]
  insumos: Insumo[]
  total_producto_kg: number
  total_sacos: number
}

export interface DashboardData {
  stock: StockResumen
  recepciones_hoy: number
  lotes_en_proceso: number
  pedidos_pendientes: number
  movimientos: Movimiento[]
  alertas: Producto[]
}

export interface ApiClient {
  login(usuario: string, password: string): Promise<Session>
  getDashboard(): Promise<DashboardData>
  getProveedores(): Promise<Proveedor[]>
  getClientes(): Promise<Cliente[]>
  getInsumos(): Promise<Insumo[]>
  getProductos(): Promise<Producto[]>
  getFormulas(): Promise<FormulaResumen[]>
  getFormula(idFormula: number, tamanoKg: number): Promise<FormulaDetalle>
  getRecepciones(): Promise<Recepcion[]>
  crearRecepcion(input: RecepcionInput): Promise<Recepcion>
  autorizarRecepcion(idRecepcion: number): Promise<Recepcion>
  getLotes(): Promise<Lote[]>
  crearLote(input: LoteInput): Promise<Lote>
  confirmarNucleo(idLote: number): Promise<Lote>
  terminarLote(idLote: number, input: LoteRealInput): Promise<Lote>
  getStock(): Promise<StockResumen>
  getKardex(): Promise<Movimiento[]>
  getPedidos(): Promise<Pedido[]>
  crearPedido(input: PedidoInput): Promise<Pedido>
  getDespachos(): Promise<Despacho[]>
  crearDespacho(input: DespachoInput): Promise<Despacho>
  confirmarDespacho(idDespacho: number): Promise<Despacho>
}
