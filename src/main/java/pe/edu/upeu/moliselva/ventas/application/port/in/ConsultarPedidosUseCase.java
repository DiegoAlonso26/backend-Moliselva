package pe.edu.upeu.moliselva.ventas.application.port.in;

import java.util.List;
import pe.edu.upeu.moliselva.ventas.domain.Pedido;
import pe.edu.upeu.moliselva.ventas.domain.PedidoDetalle;

public interface ConsultarPedidosUseCase {
    List<Pedido> listar();
    Pedido obtener(long id);
    List<PedidoDetalle> listarDetalles(long id);
    PedidoDetalle obtenerDetalle(long id, long productoId);
}
