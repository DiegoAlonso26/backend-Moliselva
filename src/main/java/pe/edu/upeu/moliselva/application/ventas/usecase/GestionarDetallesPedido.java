package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.CatalogoVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.PedidoGateway;
import pe.edu.upeu.moliselva.domain.ventas.DatosDetalle;
import pe.edu.upeu.moliselva.domain.ventas.PedidoDetalle;

public class GestionarDetallesPedido implements GestionarDetallesPedidoUseCase {
    private final PedidoGateway pedidos;
    private final CatalogoVentaGateway catalogo;
    private final BitacoraVentaGateway bitacora;
    public GestionarDetallesPedido(PedidoGateway pedidos, CatalogoVentaGateway catalogo,
            BitacoraVentaGateway bitacora) {
        this.pedidos=pedidos; this.catalogo=catalogo; this.bitacora=bitacora;
    }
    @Override
    public PedidoDetalle agregar(long id, DatosDetalle datos, long usuarioId) {
        editable(id);
        VentaChecks.require(catalogo.productoActivo(datos.idProducto()),"REFERENCIA_INVALIDA",
            "El registro relacionado no existe o está inactivo");
        var detalle=pedidos.agregarDetalle(id,datos);
        bitacora.registrar(usuarioId,"CREAR_PEDIDO_DETALLE id="+id+"/"+datos.idProducto());
        return detalle;
    }
    @Override
    public PedidoDetalle actualizar(long id, long productoId, DatosDetalle datos, long usuarioId) {
        mismoProducto(productoId,datos);
        editable(id);
        pedidos.buscarDetalle(id,productoId).orElseThrow(() -> VentaChecks.missing("PEDIDO_DETALLE"));
        var detalle=pedidos.actualizarDetalle(id,productoId,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_PEDIDO_DETALLE id="+id+"/"+productoId);
        return detalle;
    }
    @Override
    public void eliminar(long id, long productoId, long usuarioId) {
        editable(id);
        pedidos.buscarDetalle(id,productoId).orElseThrow(() -> VentaChecks.missing("PEDIDO_DETALLE"));
        pedidos.eliminarDetalle(id,productoId);
        bitacora.registrar(usuarioId,"ELIMINAR_PEDIDO_DETALLE id="+id+"/"+productoId);
    }
    private void editable(long id) {
        VentaChecks.pendiente(pedidos.bloquear(id).orElseThrow(() -> VentaChecks.missing("PEDIDO")));
    }
    private void mismoProducto(long productoId, DatosDetalle datos) {
        VentaChecks.require(datos.idProducto()==productoId,"DATOS_INVALIDOS",
            "El idProducto del cuerpo debe coincidir con la ruta");
    }
}
