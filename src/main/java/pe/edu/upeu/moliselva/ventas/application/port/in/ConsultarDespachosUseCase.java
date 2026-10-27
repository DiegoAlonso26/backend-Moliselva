package pe.edu.upeu.moliselva.ventas.application.port.in;

import java.util.List;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;
import pe.edu.upeu.moliselva.ventas.domain.DespachoDetalle;

public interface ConsultarDespachosUseCase {
    List<Despacho> listar();
    Despacho obtener(long id);
    List<DespachoDetalle> listarDetalles(long id);
    DespachoDetalle obtenerDetalle(long id, long productoId);
}
