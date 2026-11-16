package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.DespachoGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.PedidoGateway;
import pe.edu.upeu.moliselva.domain.ventas.Despacho;
import pe.edu.upeu.moliselva.domain.ventas.DespachoPolicy;

public class ConfirmarDespacho implements ConfirmarDespachoUseCase {
    private final DespachoGateway despachos;
    private final PedidoGateway pedidos;
    private final BitacoraVentaGateway bitacora;
    public ConfirmarDespacho(DespachoGateway despachos, PedidoGateway pedidos,
            BitacoraVentaGateway bitacora) {
        this.despachos=despachos; this.pedidos=pedidos; this.bitacora=bitacora;
    }
    @Override
    public Despacho confirmar(long id, long usuarioId) {
        var despacho=despachos.bloquear(id).orElseThrow(() -> VentaChecks.missing("DESPACHO"));
        VentaChecks.borrador(despacho);
        var pedido=pedidos.bloquear(despacho.idPedido()).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        if (!DespachoPolicy.pedidoAdmiteDespacho(pedido)) {
            throw VentaChecks.locked("El pedido ya no admite un despacho");
        }
        if (despachos.excedePedido(pedido.idPedido(),id)) {
            throw VentaChecks.locked("El despacho excede los productos o cantidades del pedido");
        }
        despachos.confirmarEnBase(id,usuarioId);
        bitacora.registrar(usuarioId,"CONFIRMAR_DESPACHO id="+id);
        return despachos.buscar(id).orElseThrow(() -> VentaChecks.missing("DESPACHO"));
    }
}
