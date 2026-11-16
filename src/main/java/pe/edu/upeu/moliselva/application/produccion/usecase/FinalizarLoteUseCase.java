package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.domain.produccion.Lote;

public interface FinalizarLoteUseCase {
    Lote finalizar(long id, long usuarioId);
}
