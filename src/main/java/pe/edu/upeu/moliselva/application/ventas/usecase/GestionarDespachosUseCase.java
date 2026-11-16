package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.domain.ventas.DatosDespacho;
import pe.edu.upeu.moliselva.domain.ventas.Despacho;

public interface GestionarDespachosUseCase {
    Despacho crear(DatosDespacho datos, long usuarioId);
    Despacho actualizar(long id, DatosDespacho datos, long usuarioId);
    void eliminar(long id, long usuarioId);
}
