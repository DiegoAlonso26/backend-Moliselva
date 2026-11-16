package pe.edu.upeu.moliselva.application.ventas.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.ventas.gateway.PedidoGateway;
import pe.edu.upeu.moliselva.domain.ventas.Pedido;
import pe.edu.upeu.moliselva.domain.ventas.PedidoDetalle;

public class ConsultarPedidos implements ConsultarPedidosUseCase {
    private final PedidoGateway pedidos;
    public ConsultarPedidos(PedidoGateway pedidos) { this.pedidos=pedidos; }
    @Override
    public List<Pedido> listar() { return pedidos.listar(); }
    @Override
    public Pedido obtener(long id) { return pedidos.buscar(id).orElseThrow(() -> VentaChecks.missing("PEDIDO")); }
    @Override
    public List<PedidoDetalle> listarDetalles(long id) {
        obtener(id);
        return pedidos.listarDetalles(id);
    }
    @Override
    public PedidoDetalle obtenerDetalle(long id, long productoId) {
        return pedidos.buscarDetalle(id,productoId)
            .orElseThrow(() -> VentaChecks.missing("PEDIDO_DETALLE"));
    }
}
