package pe.edu.upeu.moliselva.produccion.application.port.in;

import pe.edu.upeu.moliselva.produccion.domain.EstadoLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;

public interface CambiarEstadoLoteUseCase {
    Lote cambiar(long id, EstadoLote estado, long usuarioId);
}
