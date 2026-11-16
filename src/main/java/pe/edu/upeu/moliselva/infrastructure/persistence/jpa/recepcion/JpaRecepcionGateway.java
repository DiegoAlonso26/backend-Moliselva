package pe.edu.upeu.moliselva.infrastructure.persistence.jpa.recepcion;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;

/**
 * Lado de escritura de la recepción con JPA. Cada operación hace {@code flush} para que
 * el SQL se ejecute en el mismo orden que el caso de uso (antes de la entrada al kardex,
 * la bitácora y la lectura de la respuesta, que van por JDBC en la misma transacción).
 */
@Repository
public class JpaRecepcionGateway implements RecepcionGateway {
    private final RecepcionJpaRepository filas;

    public JpaRecepcionGateway(RecepcionJpaRepository filas) { this.filas = filas; }

    @Override
    public long agregar(Recepcion recepcion) {
        return filas.saveAndFlush(RecepcionJpaMapper.nueva(recepcion)).getId();
    }

    @Override
    public Optional<Recepcion> bloquear(long id) {
        return filas.bloquear(id).map(RecepcionJpaMapper::aDominio);
    }

    @Override
    public void guardar(Recepcion recepcion) {
        var fila = filas.findById(recepcion.id()).orElseThrow(() ->
            new IllegalStateException("La recepción " + recepcion.id() + " no fue cargada antes de guardarla"));
        RecepcionJpaMapper.copiar(recepcion, fila);
        filas.flush();
    }

    @Override
    public void eliminar(Recepcion recepcion) {
        filas.deleteById(recepcion.id());
        filas.flush();
    }
}
