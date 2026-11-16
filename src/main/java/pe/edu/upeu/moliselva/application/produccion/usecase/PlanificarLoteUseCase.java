package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.domain.produccion.DatosLote;
import pe.edu.upeu.moliselva.domain.produccion.Lote;

public interface PlanificarLoteUseCase {
    Lote crear(DatosLote datos, long usuarioId);
    Lote actualizar(long id, DatosLote datos, long usuarioId);
    void eliminar(long id, long usuarioId);
}
