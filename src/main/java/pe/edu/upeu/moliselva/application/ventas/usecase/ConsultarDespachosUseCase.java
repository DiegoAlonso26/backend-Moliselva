package pe.edu.upeu.moliselva.application.ventas.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.ventas.Despacho;
import pe.edu.upeu.moliselva.domain.ventas.DespachoDetalle;

public interface ConsultarDespachosUseCase {
    List<Despacho> listar();
    Despacho obtener(long id);
    List<DespachoDetalle> listarDetalles(long id);
    DespachoDetalle obtenerDetalle(long id, long productoId);
}
