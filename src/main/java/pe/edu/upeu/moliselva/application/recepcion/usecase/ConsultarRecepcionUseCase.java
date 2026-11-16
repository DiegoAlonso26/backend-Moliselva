package pe.edu.upeu.moliselva.application.recepcion.usecase;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;

public interface ConsultarRecepcionUseCase {
    List<Recepcion> listar(LocalDate fecha);
    Recepcion obtener(long id);
}
