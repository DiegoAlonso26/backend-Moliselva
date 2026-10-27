package pe.edu.upeu.moliselva.produccion.application.port.in;

import pe.edu.upeu.moliselva.produccion.domain.DatosInsumo;
import pe.edu.upeu.moliselva.produccion.domain.LoteInsumo;

public interface GestionarInsumosLoteUseCase {
    LoteInsumo agregar(long loteId, DatosInsumo datos, long usuarioId);
    LoteInsumo actualizar(long loteId, long insumoId, DatosInsumo datos, long usuarioId);
    void eliminar(long loteId, long insumoId, long usuarioId);
}
