package pe.edu.upeu.moliselva.application.recepcion.gateway;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;

/**
 * Lecturas de recepción para las queries (lado de lectura): devuelve directamente la
 * respuesta, sin pasar por la entidad. Lo implementa infrastructure con SQL (JDBC).
 */
public interface RecepcionConsultaGateway {
    Optional<RecepcionResponse> buscar(long id);

    List<RecepcionResponse> listar(LocalDate fecha);
}
