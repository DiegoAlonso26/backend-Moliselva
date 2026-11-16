package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.domain.ventas.DatosDetalle;
import pe.edu.upeu.moliselva.domain.ventas.PedidoDetalle;

public interface GestionarDetallesPedidoUseCase {
    PedidoDetalle agregar(long id, DatosDetalle datos, long usuarioId);
    PedidoDetalle actualizar(long id, long productoId, DatosDetalle datos, long usuarioId);
    void eliminar(long id, long productoId, long usuarioId);
}
