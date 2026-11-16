package pe.edu.upeu.moliselva.application.recepcion.gateway;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.recepcion.EstadoRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionBloqueo;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionDatos;

public interface RecepcionGateway {
    long insertar(RecepcionDatos datos, long idInsumo, EstadoRecepcion estado, long idRegistra);
    Optional<Recepcion> buscar(long id);
    List<Recepcion> listar(LocalDate fecha);
    Optional<RecepcionBloqueo> bloquear(long id);
    boolean decidir(long id, EstadoRecepcion estado, long usuarioId);
    boolean corregir(long id, RecepcionDatos datos, EstadoRecepcion estado);
    boolean eliminarEditable(long id);
}
