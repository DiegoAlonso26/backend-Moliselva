package pe.edu.upeu.moliselva.produccion.application.port.in;

import pe.edu.upeu.moliselva.produccion.domain.DatosLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;

public interface PlanificarLoteUseCase {
    Lote crear(DatosLote datos, long usuarioId);
    Lote actualizar(long id, DatosLote datos, long usuarioId);
    void eliminar(long id, long usuarioId);
}
