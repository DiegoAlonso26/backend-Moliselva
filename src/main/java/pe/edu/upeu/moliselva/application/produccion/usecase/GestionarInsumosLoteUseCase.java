package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.domain.produccion.DatosInsumo;
import pe.edu.upeu.moliselva.domain.produccion.LoteInsumo;

public interface GestionarInsumosLoteUseCase {
    LoteInsumo agregar(long loteId, DatosInsumo datos, long usuarioId);
    LoteInsumo actualizar(long loteId, long insumoId, DatosInsumo datos, long usuarioId);
    void eliminar(long loteId, long insumoId, long usuarioId);
}
