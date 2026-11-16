package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.inventario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizConsultaGateway;
import pe.edu.upeu.moliselva.application.inventario.kardex.MovimientoKardexResponse;

@Repository
/** Lado de lectura del kardex de maíz: SQL sobre las vistas vw_kardex y vw_stock_actual. */
public class JdbcKardexMaizGateway implements KardexMaizConsultaGateway {
    private final JdbcTemplate db;

    public JdbcKardexMaizGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public Optional<Long> maizId() {
        return db.query("SELECT id_insumo FROM insumo WHERE nombre='Maíz' AND activo=true",
            (rs,row) -> rs.getLong(1)).stream().findFirst();
    }

    @Override
    public List<MovimientoKardexResponse> movimientos(long idMaiz) {
        return db.query("SELECT k.id_movimiento,k.fecha_hora,k.tipo_movimiento,k.cantidad,k.saldo," +
            "k.id_recepcion,u.usuario FROM vw_kardex k JOIN usuario u ON u.id_usuario=k.id_usuario " +
            "WHERE k.tipo_item='INSUMO' AND k.id_item=? ORDER BY k.fecha_hora,k.id_movimiento",
            (rs,row) -> new MovimientoKardexResponse(rs.getLong("id_movimiento"),
                rs.getTimestamp("fecha_hora").toLocalDateTime(),rs.getString("tipo_movimiento"),
                rs.getBigDecimal("cantidad"),rs.getBigDecimal("saldo"),
                rs.getObject("id_recepcion",Long.class),rs.getString("usuario")),idMaiz);
    }

    @Override
    public BigDecimal stock(long idMaiz) {
        var rows = db.query("SELECT stock_actual FROM vw_stock_actual WHERE tipo_item='INSUMO' AND id_item=?",
            (rs,row) -> rs.getBigDecimal("stock_actual"),idMaiz);
        return rows.isEmpty() ? BigDecimal.ZERO : rows.get(0);
    }
}
