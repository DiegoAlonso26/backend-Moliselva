package pe.edu.upeu.moliselva.produccion.adapter.out.jdbc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.operaciones.OperacionRepository;
import pe.edu.upeu.moliselva.produccion.application.port.out.LotePort;
import pe.edu.upeu.moliselva.produccion.domain.DatosInsumo;
import pe.edu.upeu.moliselva.produccion.domain.DatosLote;
import pe.edu.upeu.moliselva.produccion.domain.EstadoLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;
import pe.edu.upeu.moliselva.produccion.domain.LoteInsumo;

@Repository
public class JdbcLoteAdapter implements LotePort {
    private final OperacionRepository db;
    public JdbcLoteAdapter(OperacionRepository db) { this.db=db; }

    private static Lote lote(Map<String,Object> row) {
        return new Lote(((Number)row.get("id_lote")).longValue(),
            ((Number)row.get("id_producto")).longValue(),
            ((Number)row.get("id_formula")).longValue(),
            ((Timestamp)row.get("fecha_hora")).toLocalDateTime(),
            ((Number)row.get("tamano_kg")).intValue(),
            EstadoLote.valueOf((String)row.get("estado")),
            ((Number)row.get("id_responsable")).longValue());
    }
    private static LoteInsumo insumo(Map<String,Object> row) {
        return new LoteInsumo(((Number)row.get("id_lote")).longValue(),
            ((Number)row.get("id_insumo")).longValue(),
            (BigDecimal)row.get("cantidad_calculada_kg"),
            (BigDecimal)row.get("cantidad_real_kg"));
    }
    @Override public List<Lote> listar() {
        return db.list("SELECT * FROM lote ORDER BY id_lote DESC LIMIT 500").stream()
            .map(JdbcLoteAdapter::lote).toList();
    }
    @Override public Optional<Lote> buscar(long id) {
        return db.one("SELECT * FROM lote WHERE id_lote=?",id).map(JdbcLoteAdapter::lote);
    }
    @Override public Optional<Lote> bloquear(long id) {
        return db.one("SELECT * FROM lote WHERE id_lote=? FOR UPDATE",id).map(JdbcLoteAdapter::lote);
    }
    @Override public Lote crear(DatosLote datos, long usuarioId) {
        return lote(db.insert("INSERT INTO lote(id_producto,id_formula,tamano_kg,id_responsable) " +
            "VALUES (?,?,?,?) RETURNING *",datos.idProducto(),datos.idFormula(),datos.tamanoKg(),usuarioId));
    }
    @Override public Lote actualizar(long id, DatosLote datos) {
        return lote(db.insert("UPDATE lote SET id_producto=?,id_formula=?,tamano_kg=? " +
            "WHERE id_lote=? RETURNING *",datos.idProducto(),datos.idFormula(),datos.tamanoKg(),id));
    }
    @Override public void eliminar(long id) { db.update("DELETE FROM lote WHERE id_lote=?",id); }
    @Override public List<LoteInsumo> listarInsumos(long id) {
        return db.list("SELECT * FROM lote_insumo WHERE id_lote=? ORDER BY id_insumo",id)
            .stream().map(JdbcLoteAdapter::insumo).toList();
    }
    @Override public Optional<LoteInsumo> buscarInsumo(long id, long insumoId) {
        return db.one("SELECT * FROM lote_insumo WHERE id_lote=? AND id_insumo=?",id,insumoId)
            .map(JdbcLoteAdapter::insumo);
    }
    @Override public LoteInsumo agregarInsumo(long id, DatosInsumo datos) {
        return insumo(db.insert("INSERT INTO lote_insumo(id_lote,id_insumo,cantidad_calculada_kg," +
            "cantidad_real_kg) VALUES (?,?,?,?) RETURNING *",id,datos.idInsumo(),
            datos.cantidadCalculadaKg(),datos.cantidadRealKg()));
    }
    @Override public LoteInsumo actualizarInsumo(long id, long insumoId, DatosInsumo datos) {
        return insumo(db.insert("UPDATE lote_insumo SET cantidad_calculada_kg=?,cantidad_real_kg=? " +
            "WHERE id_lote=? AND id_insumo=? RETURNING *",
            datos.cantidadCalculadaKg(),datos.cantidadRealKg(),id,insumoId));
    }
    @Override public void eliminarInsumo(long id, long insumoId) {
        db.update("DELETE FROM lote_insumo WHERE id_lote=? AND id_insumo=?",id,insumoId);
    }
    @Override public boolean tieneInsumos(long id) {
        return db.one("SELECT 1 FROM lote_insumo WHERE id_lote=?",id).isPresent();
    }
    @Override public boolean faltanInsumosFormula(long id, long formulaId) {
        return db.one("SELECT 1 FROM formula_detalle fd LEFT JOIN lote_insumo li " +
            "ON li.id_lote=? AND li.id_insumo=fd.id_insumo " +
            "WHERE fd.id_formula=? AND li.id_insumo IS NULL",id,formulaId).isPresent();
    }
    @Override public Lote cambiarEstado(long id, EstadoLote estado, long usuarioId) {
        return lote(db.insert("UPDATE lote SET estado=?,id_responsable=? WHERE id_lote=? RETURNING *",
            estado.name(),usuarioId,id));
    }
    @Override public void marcarTerminado(long id, long usuarioId) {
        db.update("UPDATE lote SET estado='TERMINADO',id_responsable=? WHERE id_lote=?",usuarioId,id);
    }
}
