package pe.edu.upeu.moliselva.ventas.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.ventas.application.port.in.GestionarPedidosUseCase;
import pe.edu.upeu.moliselva.ventas.application.port.out.BitacoraVentaPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.CatalogoVentaPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.PedidoPort;
import pe.edu.upeu.moliselva.ventas.domain.DatosPedido;
import pe.edu.upeu.moliselva.ventas.domain.EstadoPedido;
import pe.edu.upeu.moliselva.ventas.domain.Pedido;
import pe.edu.upeu.moliselva.ventas.domain.DespachoPolicy;

@Service
public class GestionarPedidos implements GestionarPedidosUseCase {
    private final PedidoPort pedidos;
    private final CatalogoVentaPort catalogo;
    private final BitacoraVentaPort bitacora;
    public GestionarPedidos(PedidoPort pedidos, CatalogoVentaPort catalogo,
            BitacoraVentaPort bitacora) {
        this.pedidos=pedidos; this.catalogo=catalogo; this.bitacora=bitacora;
    }
    @Override @Transactional
    public Pedido crear(DatosPedido datos, long usuarioId) {
        clienteActivo(datos.idCliente());
        var pedido=pedidos.crear(datos);
        bitacora.registrar(usuarioId,"CREAR_PEDIDO id="+pedido.idPedido());
        return pedido;
    }
    @Override @Transactional
    public Pedido actualizar(long id, DatosPedido datos, long usuarioId) {
        var pedido=pedidos.bloquear(id).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        VentaChecks.pendiente(pedido);
        clienteActivo(datos.idCliente());
        var actualizado=pedidos.actualizar(id,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_PEDIDO id="+id);
        return actualizado;
    }
    @Override @Transactional
    public void eliminar(long id, long usuarioId) {
        var pedido=pedidos.bloquear(id).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        VentaChecks.pendiente(pedido);
        if (pedidos.tieneDespachos(id)) { throw VentaChecks.locked("El pedido ya tiene un despacho asociado"); }
        pedidos.eliminar(id);
        bitacora.registrar(usuarioId,"ELIMINAR_PEDIDO id="+id);
    }
    @Override @Transactional
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
