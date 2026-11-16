package pe.edu.upeu.moliselva.infrastructure.persistence.jpa.inventario;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.domain.shared.Kilogramos;

/**
 * Entrada de maíz al kardex (escritura con JPA). Si el trigger de stock rechaza el
 * movimiento, el error se propaga y la transacción del command se revierte entera.
 */
@Repository
public class JpaEntradaKardexGateway implements EntradaKardexGateway {
    private final MovimientoInventarioJpaRepository movimientos;
    private final JdbcTemplate db;

    public JpaEntradaKardexGateway(MovimientoInventarioJpaRepository movimientos, JdbcTemplate db) {
        this.movimientos = movimientos;
        this.db = db;
    }

    @Override
    public Optional<Long> maizId() {
        return db.query("SELECT id_insumo FROM insumo WHERE nombre='Maíz' AND activo=true",
            (rs,row) -> rs.getLong(1)).stream().findFirst();
    }

    @Override
    public void registrarEntrada(long idInsumo, long idRecepcion, Kilogramos cantidad, long idUsuario) {
        movimientos.saveAndFlush(MovimientoInventarioJpa.entradaPorRecepcion(idInsumo, idRecepcion,
            cantidad.valor(), idUsuario));
    }
}
