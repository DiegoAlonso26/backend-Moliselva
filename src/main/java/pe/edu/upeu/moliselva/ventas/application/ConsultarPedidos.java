package pe.edu.upeu.moliselva.ventas.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.ventas.application.port.in.ConsultarPedidosUseCase;
import pe.edu.upeu.moliselva.ventas.application.port.out.PedidoPort;
import pe.edu.upeu.moliselva.ventas.domain.Pedido;
import pe.edu.upeu.moliselva.ventas.domain.PedidoDetalle;

@Service
public class ConsultarPedidos implements ConsultarPedidosUseCase {
    private final PedidoPort pedidos;
    public ConsultarPedidos(PedidoPort pedidos) { this.pedidos=pedidos; }
    @Override @Transactional(readOnly=true)
    public List<Pedido> listar() { return pedidos.listar(); }
    @Override @Transactional(readOnly=true)
    public Pedido obtener(long id) { return pedidos.buscar(id).orElseThrow(() -> VentaChecks.missing("PEDIDO")); }
    @Override @Transactional(readOnly=true)
    public List<PedidoDetalle> listarDetalles(long id) {
        obtener(id);
        return pedidos.listarDetalles(id);
    }
    @Override @Transactional(readOnly=true)
    public PedidoDetalle obtenerDetalle(long id, long productoId) {
        return pedidos.buscarDetalle(id,productoId)
            .orElseThrow(() -> VentaChecks.missing("PEDIDO_DETALLE"));
    }
}
