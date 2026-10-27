package pe.edu.upeu.moliselva.operaciones;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.comun.ApiException;
import pe.edu.upeu.moliselva.comun.BitacoraService;

@Service
public class OperacionService {
    private final OperacionRepository db;
    private final BitacoraService audit;
    public OperacionService(OperacionRepository db, BitacoraService audit) {
        this.db=db; this.audit=audit;
    }

    private Map<String,Object> camel(Map<String,Object> row) {
        Map<String,Object> out=new LinkedHashMap<>();
        row.forEach((key,value) -> {
            StringBuilder name=new StringBuilder();
            boolean upper=false;
            for (char c : key.toCharArray()) {
                if (c=='_') { upper=true; continue; }
                name.append(upper ? Character.toUpperCase(c) : c);
                upper=false;
            }
            out.put(name.toString(),value instanceof Timestamp t ? t.toLocalDateTime() : value);
        });
        return out;
    }
    private List<Map<String,Object>> list(String sql, Object... args) {
        return db.list(sql,args).stream().map(this::camel).toList();
    }
    private Map<String,Object> find(String type, String sql, Object... args) {
        return db.one(sql,args).map(this::camel).orElseThrow(() -> missing(type));
    }
    private Map<String,Object> write(String sql, Object... args) { return camel(db.insert(sql,args)); }
    private boolean exists(String sql, Object... args) { return db.one(sql,args).isPresent(); }
    private ApiException missing(String type) {
        return new ApiException(HttpStatus.NOT_FOUND,type+"_NO_ENCONTRADO","El registro no existe");
    }
    private ApiException locked(String message) {
        return new ApiException(HttpStatus.CONFLICT,"REGISTRO_NO_EDITABLE",message);
    }
    private void require(boolean test, String code, String message) {
        if (!test) { throw new ApiException(HttpStatus.BAD_REQUEST,code,message); }
    }
    private void log(long userId, String action, Object id) {
        audit.registrar(userId,action+" id="+id,null);
    }
    private void active(String table, String idColumn, long id) {
        require(exists("SELECT 1 FROM "+table+" WHERE "+idColumn+"=? AND activo=true",id),
            "REFERENCIA_INVALIDA","El registro relacionado no existe o está inactivo");
    }

    public List<Map<String,Object>> formulas() { return list("SELECT * FROM formula ORDER BY id_formula DESC LIMIT 500"); }
    public Map<String,Object> formula(long id) { return find("FORMULA","SELECT * FROM formula WHERE id_formula=?",id); }
    @Transactional
    public Map<String,Object> crearFormula(OperacionDto.Formula d, long userId) {
        active("producto","id_producto",d.idProducto());
        var row=write("INSERT INTO formula(id_producto,version,vigente,id_creador) VALUES (?,?,?,?) RETURNING *",
            d.idProducto(),d.version().trim(),d.vigente()==null ? true : d.vigente(),userId);
        log(userId,"CREAR_FORMULA",row.get("idFormula")); return row;
    }
    private void formulaEditable(long id) {
        formula(id);
        if (exists("SELECT 1 FROM lote WHERE id_formula=?",id)) {
            throw locked("La fórmula ya está utilizada por un lote");
        }
    }
    @Transactional
    public Map<String,Object> actualizarFormula(long id, OperacionDto.Formula d, long userId) {
        formulaEditable(id); active("producto","id_producto",d.idProducto());
        var row=write("UPDATE formula SET id_producto=?,version=?,vigente=? WHERE id_formula=? RETURNING *",
            d.idProducto(),d.version().trim(),d.vigente()==null ? true : d.vigente(),id);
        log(userId,"ACTUALIZAR_FORMULA",id); return row;
    }
    @Transactional
    public void eliminarFormula(long id, long userId) {
        formulaEditable(id);
        db.update("DELETE FROM formula WHERE id_formula=?",id);
        log(userId,"ELIMINAR_FORMULA",id);
    }
    @Transactional
    public Map<String,Object> vigenciaFormula(long id, OperacionDto.VigenciaFormula d, long userId) {
        formula(id);
        var row=write("UPDATE formula SET vigente=? WHERE id_formula=? RETURNING *",d.vigente(),id);
        log(userId,"CAMBIAR_VIGENCIA_FORMULA",id+"/"+d.vigente()); return row;
    }
    public List<Map<String,Object>> formulaDetalles(long id) {
        formula(id);
        return list("SELECT * FROM formula_detalle WHERE id_formula=? ORDER BY id_insumo",id);
    }
    public Map<String,Object> formulaDetalle(long id, long insumoId) {
        return find("FORMULA_DETALLE","SELECT * FROM formula_detalle WHERE id_formula=? AND id_insumo=?",id,insumoId);
    }
    @Transactional
    public Map<String,Object> crearFormulaDetalle(long id, OperacionDto.FormulaDetalle d, long userId) {
        formulaEditable(id); active("insumo","id_insumo",d.idInsumo());
        var row=write("INSERT INTO formula_detalle(id_formula,id_insumo,proporcion_confidencial) " +
            "VALUES (?,?,?) RETURNING *",id,d.idInsumo(),d.proporcionConfidencial());
        log(userId,"CREAR_FORMULA_DETALLE",id+"/"+d.idInsumo()); return row;
    }
    @Transactional
    public Map<String,Object> actualizarFormulaDetalle(long id, long insumoId,
            OperacionDto.FormulaDetalle d, long userId) {
        require(d.idInsumo()==insumoId,"DATOS_INVALIDOS","El idInsumo del cuerpo debe coincidir con la ruta");
        formulaEditable(id); formulaDetalle(id,insumoId);
        var row=write("UPDATE formula_detalle SET proporcion_confidencial=? " +
            "WHERE id_formula=? AND id_insumo=? RETURNING *",d.proporcionConfidencial(),id,insumoId);
        log(userId,"ACTUALIZAR_FORMULA_DETALLE",id+"/"+insumoId); return row;
    }
    @Transactional
    public void eliminarFormulaDetalle(long id, long insumoId, long userId) {
        formulaEditable(id); formulaDetalle(id,insumoId);
        db.update("DELETE FROM formula_detalle WHERE id_formula=? AND id_insumo=?",id,insumoId);
        log(userId,"ELIMINAR_FORMULA_DETALLE",id+"/"+insumoId);
    }

}
