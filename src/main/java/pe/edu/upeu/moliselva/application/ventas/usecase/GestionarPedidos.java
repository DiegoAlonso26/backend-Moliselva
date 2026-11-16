package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.CatalogoVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.PedidoGateway;
import pe.edu.upeu.moliselva.domain.ventas.DatosPedido;
import pe.edu.upeu.moliselva.domain.ventas.EstadoPedido;
import pe.edu.upeu.moliselva.domain.ventas.Pedido;
import pe.edu.upeu.moliselva.domain.ventas.DespachoPolicy;

public class GestionarPedidos implements GestionarPedidosUseCase {
    private final PedidoGateway pedidos;
    private final CatalogoVentaGateway catalogo;
    private final BitacoraVentaGateway bitacora;
    public GestionarPedidos(PedidoGateway pedidos, CatalogoVentaGateway catalogo,
            BitacoraVentaGateway bitacora) {
        this.pedidos=pedidos; this.catalogo=catalogo; this.bitacora=bitacora;
    }
    @Override
    public Pedido crear(DatosPedido datos, long usuarioId) {
        clienteActivo(datos.idCliente());
        var pedido=pedidos.crear(datos);
        bitacora.registrar(usuarioId,"CREAR_PEDIDO id="+pedido.idPedido());
        return pedido;
    }
    @Override
    public Pedido actualizar(long id, DatosPedido datos, long usuarioId) {
        var pedido=pedidos.bloquear(id).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        VentaChecks.pendiente(pedido);
        clienteActivo(datos.idCliente());
        var actualizado=pedidos.actualizar(id,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_PEDIDO id="+id);
        return actualizado;
    }
    @Override
    public void eliminar(long id, long usuarioId) {
        var pedido=pedidos.bloquear(id).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        VentaChecks.pendiente(pedido);
        if (pedidos.tieneDespachos(id)) { throw VentaChecks.locked("El pedido ya tiene un despacho asociado"); }
        pedidos.eliminar(id);
        bitacora.registrar(usuarioId,"ELIMINAR_PEDIDO id="+id);
    }
    @Override
    public Pedido cambiarEstado(long id, EstadoPedido estado, long usuarioId) {
        var pedido=pedidos.bloquear(id).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        if (estado==EstadoPedido.EN_PREPARACION) {
            if (pedido.estado()!=EstadoPedido.PENDIENTE || !pedidos.tieneDetalles(id)) {
                throw VentaChecks.locked("El pedido debe estar pendiente y tener detalles para prepararlo");
            }
        } else if (estado==EstadoPedido.CANCELADO) {
            if (!DespachoPolicy.pedidoCancelable(pedido) || pedidos.tieneDespachos(id)) {
                throw VentaChecks.locked("El pedido no se puede cancelar si tiene despachos");
            }
        } else {
            VentaChecks.require(false,"DATOS_INVALIDOS","Estado de pedido no permitido");
        }
        var actualizado=pedidos.cambiarEstado(id,estado);
        bitacora.registrar(usuarioId,"CAMBIAR_ESTADO_PEDIDO id="+id+"/"+estado);
        return actualizado;
    }
    private void clienteActivo(long id) {
        VentaChecks.require(catalogo.clienteActivo(id),"REFERENCIA_INVALIDA",
            "El registro relacionado no existe o está inactivo");
    }
}
