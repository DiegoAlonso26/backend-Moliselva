package pe.edu.upeu.moliselva.ventas.application.port.in;

import pe.edu.upeu.moliselva.ventas.domain.DatosDespacho;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;

public interface GestionarDespachosUseCase {
    Despacho crear(DatosDespacho datos, long usuarioId);
    Despacho actualizar(long id, DatosDespacho datos, long usuarioId);
    void eliminar(long id, long usuarioId);
}
