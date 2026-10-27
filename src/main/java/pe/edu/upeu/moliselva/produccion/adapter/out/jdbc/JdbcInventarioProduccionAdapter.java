package pe.edu.upeu.moliselva.produccion.adapter.out.jdbc;

import java.math.BigDecimal;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.operaciones.OperacionRepository;
import pe.edu.upeu.moliselva.produccion.application.ProduccionFailure;
import pe.edu.upeu.moliselva.produccion.application.port.out.InventarioProduccionPort;

@Repository
public class JdbcInventarioProduccionAdapter implements InventarioProduccionPort {
    private final OperacionRepository db;
    public JdbcInventarioProduccionAdapter(OperacionRepository db) { this.db=db; }
    @Override public boolean tieneMovimientos(long idLote) {
        return db.one("SELECT 1 FROM movimiento_inventario WHERE id_lote=?",idLote).isPresent();
    }
    @Override public void registrarSalida(long idLote, long idInsumo,
            BigDecimal cantidad, long usuarioId) {
        try {
            db.insert("INSERT INTO movimiento_inventario(tipo_movimiento,id_insumo,cantidad," +
                "unidad_medida,id_lote,id_usuario) SELECT 'SALIDA',?, ?,unidad_medida,?,? " +
                "FROM insumo WHERE id_insumo=? RETURNING id_movimiento",
                idInsumo,cantidad,idLote,usuarioId,idInsumo);
        } catch (DataAccessException ex) {
            if (ex.getMessage()!=null && ex.getMessage().contains("Stock insuficiente")) {
                throw new ProduccionFailure(409,"STOCK_INSUFICIENTE",
                    "No hay insumos suficientes para finalizar el lote");
            }
            throw ex;
        }
    }
    @Override public void registrarEntrada(long idLote, long idProducto,
            int tamanoKg, long usuarioId) {
        db.insert("INSERT INTO movimiento_inventario(tipo_movimiento,id_producto,cantidad," +
            "unidad_medida,id_lote,id_usuario) VALUES ('ENTRADA',?,?,'kg',?,?) RETURNING id_movimiento",
            idProducto,tamanoKg,idLote,usuarioId);
    }
}
