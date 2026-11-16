package pe.edu.upeu.moliselva.infrastructure.persistence.jpa.recepcion;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repositorio Spring Data de {@link RecepcionJpa}. */
public interface RecepcionJpaRepository extends JpaRepository<RecepcionJpa, Long> {
    /** {@code SELECT ... FOR UPDATE}: bloquea la fila hasta que termine la transacción. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecepcionJpa r where r.id = :id")
    Optional<RecepcionJpa> bloquear(@Param("id") Long id);
}
