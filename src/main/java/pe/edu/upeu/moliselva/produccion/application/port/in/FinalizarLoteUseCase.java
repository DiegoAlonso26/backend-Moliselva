package pe.edu.upeu.moliselva.produccion.application.port.in;

import pe.edu.upeu.moliselva.produccion.domain.Lote;

public interface FinalizarLoteUseCase {
    Lote finalizar(long id, long usuarioId);
}
