package pe.edu.upeu.moliselva.recepcion.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.recepcion.domain.EstadoRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionBloqueo;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionDatos;

public interface RecepcionPort {
    long insertar(RecepcionDatos datos, long idInsumo, EstadoRecepcion estado);
    Optional<Recepcion> buscar(long id);
    List<Recepcion> listar(LocalDate fecha);
    Optional<RecepcionBloqueo> bloquear(long id);
    boolean decidir(long id, EstadoRecepcion estado, long usuarioId);
    boolean corregir(long id, RecepcionDatos datos, EstadoRecepcion estado);
    boolean eliminarEditable(long id);
}
