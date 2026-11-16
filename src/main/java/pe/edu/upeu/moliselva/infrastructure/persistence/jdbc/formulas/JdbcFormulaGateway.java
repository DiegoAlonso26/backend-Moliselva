package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.formulas;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.formulas.gateway.FormulaGateway;
import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDatos;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;

@Repository
public class JdbcFormulaGateway implements FormulaGateway {
    private static final RowMapper<Formula> FORMULA = (rs,n) -> {
        Timestamp creacion = rs.getTimestamp("fecha_creacion");
        return new Formula(rs.getObject("id_formula",Long.class),rs.getObject("id_producto",Long.class),
            rs.getString("version"),rs.getObject("vigente",Boolean.class),rs.getObject("id_creador",Long.class),
            creacion==null ? null : creacion.toLocalDateTime());
    };
    private static final RowMapper<FormulaDetalle> DETALLE = (rs,n) -> new FormulaDetalle(
        rs.getObject("id_formula",Long.class),rs.getObject("id_insumo",Long.class),
        rs.getBigDecimal("proporcion_confidencial"));
    private final JdbcTemplate db;

    public JdbcFormulaGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public List<Formula> listar() {
        return db.query("SELECT * FROM formula ORDER BY id_formula DESC LIMIT 500",FORMULA);
    }

    @Override
    public Optional<Formula> buscar(long id) {
        return db.query("SELECT * FROM formula WHERE id_formula=?",FORMULA,id).stream().findFirst();
    }

    @Override
    public Formula crear(FormulaDatos d, long idCreador) {
        return unica(db.query("INSERT INTO formula(id_producto,version,vigente,id_creador) VALUES (?,?,?,?) RETURNING *",
            FORMULA,d.idProducto(),d.version(),d.vigente(),idCreador));
    }

    @Override
    public Formula actualizar(long id, FormulaDatos d) {
        return unica(db.query("UPDATE formula SET id_producto=?,version=?,vigente=? WHERE id_formula=? RETURNING *",
            FORMULA,d.idProducto(),d.version(),d.vigente(),id));
    }

    @Override
    public void eliminar(long id) { db.update("DELETE FROM formula WHERE id_formula=?",id); }

    @Override
    public Formula cambiarVigencia(long id, boolean vigente) {
        return unica(db.query("UPDATE formula SET vigente=? WHERE id_formula=? RETURNING *",FORMULA,vigente,id));
    }

    @Override
    public boolean usadaPorLote(long id) {
        return !db.queryForList("SELECT 1 FROM lote WHERE id_formula=?",id).isEmpty();
    }

    @Override
    public List<FormulaDetalle> listarDetalles(long id) {
        return db.query("SELECT * FROM formula_detalle WHERE id_formula=? ORDER BY id_insumo",DETALLE,id);
    }

    @Override
    public Optional<FormulaDetalle> buscarDetalle(long id, long idInsumo) {
        return db.query("SELECT * FROM formula_detalle WHERE id_formula=? AND id_insumo=?",DETALLE,id,idInsumo)
            .stream().findFirst();
    }

    @Override
    public FormulaDetalle agregarDetalle(long id, long idInsumo, BigDecimal proporcion) {
        return unica(db.query("INSERT INTO formula_detalle(id_formula,id_insumo,proporcion_confidencial) " +
            "VALUES (?,?,?) RETURNING *",DETALLE,id,idInsumo,proporcion));
    }

    @Override
    public FormulaDetalle actualizarDetalle(long id, long idInsumo, BigDecimal proporcion) {
        return unica(db.query("UPDATE formula_detalle SET proporcion_confidencial=? " +
            "WHERE id_formula=? AND id_insumo=? RETURNING *",DETALLE,proporcion,id,idInsumo));
    }

    @Override
    public void eliminarDetalle(long id, long idInsumo) {
        db.update("DELETE FROM formula_detalle WHERE id_formula=? AND id_insumo=?",id,idInsumo);
    }

    @Override
    public boolean productoActivo(long idProducto) {
        return !db.queryForList("SELECT 1 FROM producto WHERE id_producto=? AND activo=true",idProducto).isEmpty();
    }

    @Override
    public boolean insumoActivo(long idInsumo) {
        return !db.queryForList("SELECT 1 FROM insumo WHERE id_insumo=? AND activo=true",idInsumo).isEmpty();
    }

    private static <T> T unica(List<T> filas) {
        return filas.stream().findFirst().orElseThrow(() -> new IllegalStateException("INSERT sin fila devuelta"));
    }
}
