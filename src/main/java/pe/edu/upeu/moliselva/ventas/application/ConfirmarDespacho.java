package pe.edu.upeu.moliselva.ventas.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.ventas.application.port.in.ConfirmarDespachoUseCase;
import pe.edu.upeu.moliselva.ventas.application.port.out.BitacoraVentaPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.DespachoPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.PedidoPort;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;
import pe.edu.upeu.moliselva.ventas.domain.DespachoPolicy;

@Service
public class ConfirmarDespacho implements ConfirmarDespachoUseCase {
    private final DespachoPort despachos;
    private final PedidoPort pedidos;
    private final BitacoraVentaPort bitacora;
    public ConfirmarDespacho(DespachoPort despachos, PedidoPort pedidos,
            BitacoraVentaPort bitacora) {
        this.despachos=despachos; this.pedidos=pedidos; this.bitacora=bitacora;
    }
    @Override @Transactional
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
