package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.domain.ventas.Despacho;

public interface ConfirmarDespachoUseCase {
    Despacho confirmar(long id, long usuarioId);
}
