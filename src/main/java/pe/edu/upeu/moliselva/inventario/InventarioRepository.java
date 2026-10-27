package pe.edu.upeu.moliselva.inventario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventarioRepository {
    private final JdbcTemplate db;
    public InventarioRepository(JdbcTemplate db) { this.db=db; }

    public Optional<Long> maizId() {
        return db.query("SELECT id_insumo FROM insumo WHERE nombre='Maíz' AND activo=true",
            (rs,row) -> rs.getLong(1)).stream().findFirst();
    }

    public long insertarEntradaMaiz(long idInsumo, long idRecepcion, BigDecimal cantidad, long idUsuario) {
        Long id = db.queryForObject("INSERT INTO movimiento_inventario(tipo_movimiento,id_insumo,cantidad," +
            "unidad_medida,id_recepcion,id_usuario) VALUES ('ENTRADA',?,?,'kg',?,?) RETURNING id_movimiento",
            Long.class,idInsumo,cantidad,idRecepcion,idUsuario);
        return id;
    }

    public List<InventarioDto.Kardex> kardexMaiz(long idMaiz) {
        return db.query("SELECT k.id_movimiento,k.fecha_hora,k.tipo_movimiento,k.cantidad,k.saldo," +
            "k.id_recepcion,u.usuario FROM vw_kardex k JOIN usuario u ON u.id_usuario=k.id_usuario " +
            "WHERE k.tipo_item='INSUMO' AND k.id_item=? ORDER BY k.fecha_hora,k.id_movimiento",
            (rs,row) -> new InventarioDto.Kardex(rs.getLong("id_movimiento"),
                rs.getTimestamp("fecha_hora").toLocalDateTime(),rs.getString("tipo_movimiento"),
                rs.getBigDecimal("cantidad"),rs.getBigDecimal("saldo"),
                rs.getObject("id_recepcion",Long.class),rs.getString("usuario")),idMaiz);
    }

    public BigDecimal stockMaiz(long idMaiz) {
        var rows = db.query("SELECT stock_actual FROM vw_stock_actual " +
            "WHERE tipo_item='INSUMO' AND id_item=?",
            (rs,row) -> rs.getBigDecimal("stock_actual"),idMaiz);
        return rows.isEmpty() ? BigDecimal.ZERO : rows.get(0);
    }
}
