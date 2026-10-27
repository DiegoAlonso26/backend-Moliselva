package pe.edu.upeu.moliselva.ventas.application.port.in;

import pe.edu.upeu.moliselva.ventas.domain.Despacho;

public interface ConfirmarDespachoUseCase {
    Despacho confirmar(long id, long usuarioId);
}
