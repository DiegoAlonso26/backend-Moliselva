package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.domain.ventas.DatosPedido;
import pe.edu.upeu.moliselva.domain.ventas.EstadoPedido;
import pe.edu.upeu.moliselva.domain.ventas.Pedido;

public interface GestionarPedidosUseCase {
    Pedido crear(DatosPedido datos, long usuarioId);
    Pedido actualizar(long id, DatosPedido datos, long usuarioId);
    void eliminar(long id, long usuarioId);
    Pedido cambiarEstado(long id, EstadoPedido estado, long usuarioId);
}
