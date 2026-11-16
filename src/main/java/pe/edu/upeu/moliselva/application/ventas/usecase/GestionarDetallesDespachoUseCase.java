package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.domain.ventas.DatosDetalle;
import pe.edu.upeu.moliselva.domain.ventas.DespachoDetalle;

public interface GestionarDetallesDespachoUseCase {
    DespachoDetalle agregar(long id, DatosDetalle datos, long usuarioId);
    DespachoDetalle actualizar(long id, long productoId, DatosDetalle datos, long usuarioId);
    void eliminar(long id, long productoId, long usuarioId);
}
