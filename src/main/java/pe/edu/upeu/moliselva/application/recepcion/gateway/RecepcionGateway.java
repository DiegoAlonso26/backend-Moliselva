package pe.edu.upeu.moliselva.application.recepcion.gateway;

import java.util.Optional;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;

/**
 * Persistencia del agregado {@link Recepcion} para los commands (lado de escritura).
 * Lo implementa infrastructure con JPA.
 */
public interface RecepcionGateway {
    /** Inserta una recepción nueva y devuelve su id. */
    long agregar(Recepcion recepcion);

    /** Carga la recepción bloqueando su fila hasta el fin de la transacción ({@code FOR UPDATE}). */
    Optional<Recepcion> bloquear(long id);

    /** Guarda los cambios de una recepción cargada con {@link #bloquear(long)}. */
    void guardar(Recepcion recepcion);

    void eliminar(Recepcion recepcion);
}
