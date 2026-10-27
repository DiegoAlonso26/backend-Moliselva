package pe.edu.upeu.moliselva.recepcion.application.port.in;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;

public interface ConsultarRecepcionUseCase {
    List<Recepcion> listar(LocalDate fecha);
    Recepcion obtener(long id);
}
