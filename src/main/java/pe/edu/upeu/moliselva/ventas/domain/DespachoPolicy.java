package pe.edu.upeu.moliselva.ventas.domain;

public final class DespachoPolicy {
    private DespachoPolicy() {}
    public static boolean pedidoAdmiteDespacho(Pedido pedido) {
        return pedido.estado()==EstadoPedido.PENDIENTE ||
            pedido.estado()==EstadoPedido.EN_PREPARACION;
    }
    public static boolean requiereDestino(DatosDespacho datos) {
        return datos.tipoEntrega()==TipoEntrega.REPARTO && datos.destinoNormalizado()==null;
    }
    public static boolean pedidoCancelable(Pedido pedido) {
        return pedidoAdmiteDespacho(pedido);
    }
}
