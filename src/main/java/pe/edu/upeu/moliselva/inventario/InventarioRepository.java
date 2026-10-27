package pe.edu.upeu.moliselva.inventario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
    private static final RowMapper<InventarioDto.ItemStock> STOCK = (rs,row) ->
        new InventarioDto.ItemStock(rs.getString("tipo_item"),rs.getLong("id_item"),
            rs.getString("nombre"),rs.getString("unidad_medida"),rs.getBigDecimal("stock_actual"));
    public List<InventarioDto.ItemStock> stockTodos() {
        return db.query("SELECT tipo_item,id_item,nombre,unidad_medida,stock_actual " +
            "FROM vw_stock_actual ORDER BY tipo_item,nombre,id_item",STOCK);
    }
    public Optional<InventarioDto.ItemStock> stockItem(String tipo, long id) {
        return db.query("SELECT tipo_item,id_item,nombre,unidad_medida,stock_actual " +
            "FROM vw_stock_actual WHERE tipo_item=? AND id_item=?",STOCK,tipo,id).stream().findFirst();
    }

    private static final RowMapper<InventarioDto.Movimiento> MOVIMIENTO = (rs,row) ->
        new InventarioDto.Movimiento(rs.getLong("id_movimiento"),
            rs.getTimestamp("fecha_hora").toLocalDateTime(),rs.getString("tipo_movimiento"),
            rs.getObject("id_insumo",Long.class),rs.getObject("id_producto",Long.class),
            rs.getBigDecimal("cantidad"),rs.getString("unidad_medida"),
            rs.getObject("id_recepcion",Long.class),rs.getObject("id_lote",Long.class),
            rs.getObject("id_despacho",Long.class),rs.getString("usuario"));
    private static final String MOVIMIENTO_SQL = "SELECT m.id_movimiento,m.fecha_hora,m.tipo_movimiento," +
        "m.id_insumo,m.id_producto,m.cantidad,m.unidad_medida,m.id_recepcion,m.id_lote,m.id_despacho,u.usuario " +
        "FROM movimiento_inventario m JOIN usuario u ON u.id_usuario=m.id_usuario ";

    public List<InventarioDto.Movimiento> movimientos() {
        return db.query(MOVIMIENTO_SQL+"ORDER BY m.id_movimiento DESC LIMIT 500",MOVIMIENTO);
    }
    public Optional<InventarioDto.Movimiento> movimiento(long id) {
        return db.query(MOVIMIENTO_SQL+"WHERE m.id_movimiento=?",MOVIMIENTO,id).stream().findFirst();
    }
    public boolean itemActivo(Long insumo, Long producto) {
        String sql=insumo!=null ? "SELECT count(*) FROM insumo WHERE id_insumo=? AND activo=true"
            : "SELECT count(*) FROM producto WHERE id_producto=? AND activo=true";
        Integer count=db.queryForObject(sql,Integer.class,insumo!=null ? insumo : producto);
        return count!=null && count==1;
    }
    public String unidadMedida(Long insumo) {
        return insumo==null ? "kg" : db.queryForObject(
            "SELECT unidad_medida FROM insumo WHERE id_insumo=?",String.class,insumo);
    }
    public long insertarAjuste(InventarioDto.Ajuste d, String unidad, long userId) {
        Long id=db.queryForObject("INSERT INTO movimiento_inventario(tipo_movimiento,id_insumo,id_producto," +
            "cantidad,unidad_medida,id_usuario) VALUES (?,?,?,?,?,?) RETURNING id_movimiento",Long.class,
            d.tipoMovimiento(),d.idInsumo(),d.idProducto(),d.cantidad(),unidad,userId);
        return id;
    }
}
