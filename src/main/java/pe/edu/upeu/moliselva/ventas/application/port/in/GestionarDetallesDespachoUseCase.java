package pe.edu.upeu.moliselva.ventas.application.port.in;

import pe.edu.upeu.moliselva.ventas.domain.DatosDetalle;
import pe.edu.upeu.moliselva.ventas.domain.DespachoDetalle;

public interface GestionarDetallesDespachoUseCase {
    DespachoDetalle agregar(long id, DatosDetalle datos, long usuarioId);
    DespachoDetalle actualizar(long id, long productoId, DatosDetalle datos, long usuarioId);
    void eliminar(long id, long productoId, long usuarioId);
}
