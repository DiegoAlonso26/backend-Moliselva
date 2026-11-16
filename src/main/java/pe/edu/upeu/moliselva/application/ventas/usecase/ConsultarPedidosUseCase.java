package pe.edu.upeu.moliselva.application.ventas.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.ventas.Pedido;
import pe.edu.upeu.moliselva.domain.ventas.PedidoDetalle;

public interface ConsultarPedidosUseCase {
    List<Pedido> listar();
    Pedido obtener(long id);
    List<PedidoDetalle> listarDetalles(long id);
    PedidoDetalle obtenerDetalle(long id, long productoId);
}
