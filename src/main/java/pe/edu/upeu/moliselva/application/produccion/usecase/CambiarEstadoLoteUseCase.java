package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.domain.produccion.EstadoLote;
import pe.edu.upeu.moliselva.domain.produccion.Lote;

public interface CambiarEstadoLoteUseCase {
    Lote cambiar(long id, EstadoLote estado, long usuarioId);
}
