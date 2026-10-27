package pe.edu.upeu.moliselva.ventas.application.port.in;

import pe.edu.upeu.moliselva.ventas.domain.DatosPedido;
import pe.edu.upeu.moliselva.ventas.domain.EstadoPedido;
import pe.edu.upeu.moliselva.ventas.domain.Pedido;

public interface GestionarPedidosUseCase {
    Pedido crear(DatosPedido datos, long usuarioId);
    Pedido actualizar(long id, DatosPedido datos, long usuarioId);
    void eliminar(long id, long usuarioId);
    Pedido cambiarEstado(long id, EstadoPedido estado, long usuarioId);
}
