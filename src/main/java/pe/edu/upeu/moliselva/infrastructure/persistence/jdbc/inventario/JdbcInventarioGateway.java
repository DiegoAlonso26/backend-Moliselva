package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.inventario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.inventario.gateway.InventarioGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.StockItemGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.inventario.AjusteInventario;
import pe.edu.upeu.moliselva.domain.inventario.ItemStock;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoInventario;

@Repository
public class JdbcInventarioGateway implements InventarioGateway, StockItemGateway {
    private static final RowMapper<ItemStock> STOCK = (rs,row) ->
        new ItemStock(rs.getString("tipo_item"),rs.getLong("id_item"),
            rs.getString("nombre"),rs.getString("unidad_medida"),rs.getBigDecimal("stock_actual"));
    private static final RowMapper<MovimientoInventario> MOVIMIENTO = (rs,row) ->
        new MovimientoInventario(rs.getLong("id_movimiento"),
            rs.getTimestamp("fecha_hora").toLocalDateTime(),rs.getString("tipo_movimiento"),
            rs.getObject("id_insumo",Long.class),rs.getObject("id_producto",Long.class),
            rs.getBigDecimal("cantidad"),rs.getString("unidad_medida"),
            rs.getObject("id_recepcion",Long.class),rs.getObject("id_lote",Long.class),
            rs.getObject("id_despacho",Long.class),rs.getString("usuario"));
    private static final String MOVIMIENTO_SQL = "SELECT m.id_movimiento,m.fecha_hora,m.tipo_movimiento," +
        "m.id_insumo,m.id_producto,m.cantidad,m.unidad_medida,m.id_recepcion,m.id_lote,m.id_despacho,u.usuario " +
        "FROM movimiento_inventario m JOIN usuario u ON u.id_usuario=m.id_usuario ";
    private final JdbcTemplate db;

    public JdbcInventarioGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public List<ItemStock> stockTodos() {
        return db.query("SELECT tipo_item,id_item,nombre,unidad_medida,stock_actual " +
            "FROM vw_stock_actual ORDER BY tipo_item,nombre,id_item",STOCK);
    }

    @Override
    public Optional<ItemStock> stockItem(String tipo, long id) {
        return db.query("SELECT tipo_item,id_item,nombre,unidad_medida,stock_actual " +
            "FROM vw_stock_actual WHERE tipo_item=? AND id_item=?",STOCK,tipo,id).stream().findFirst();
    }

    @Override
    public Optional<BigDecimal> stockActual(String tipo, long id) {
        return stockItem(tipo,id).map(ItemStock::stockActual);
    }

    @Override
    public List<MovimientoInventario> movimientos() {
        return db.query(MOVIMIENTO_SQL+"ORDER BY m.id_movimiento DESC LIMIT 500",MOVIMIENTO);
    }

    @Override
    public Optional<MovimientoInventario> movimiento(long id) {
        return db.query(MOVIMIENTO_SQL+"WHERE m.id_movimiento=?",MOVIMIENTO,id).stream().findFirst();
    }

    @Override
    public boolean itemActivo(Long insumo, Long producto) {
        String sql=insumo!=null ? "SELECT count(*) FROM insumo WHERE id_insumo=? AND activo=true"
            : "SELECT count(*) FROM producto WHERE id_producto=? AND activo=true";
        Integer count=db.queryForObject(sql,Integer.class,insumo!=null ? insumo : producto);
        return count!=null && count==1;
    }

    @Override
    public String unidadMedida(Long insumo) {
        return insumo==null ? "kg" : db.queryForObject(
            "SELECT unidad_medida FROM insumo WHERE id_insumo=?",String.class,insumo);
    }

    @Override
    public long insertarAjuste(AjusteInventario d, String unidad, long userId) {
        try {
            Long id=db.queryForObject("INSERT INTO movimiento_inventario(tipo_movimiento,id_insumo,id_producto," +
                "cantidad,unidad_medida,id_usuario) VALUES (?,?,?,?,?,?) RETURNING id_movimiento",Long.class,
                d.tipoMovimiento(),d.idInsumo(),d.idProducto(),d.cantidad(),unidad,userId);
            return id;
        } catch (DataAccessException ex) {
            // El trigger de la base rechaza el movimiento que dejaría stock negativo.
            if (ex.getMessage()!=null && ex.getMessage().contains("Stock insuficiente")) {
                throw FallaAplicacion.conflicto("STOCK_INSUFICIENTE","El ajuste dejaría stock negativo");
            }
            throw ex;
        }
    }
}
