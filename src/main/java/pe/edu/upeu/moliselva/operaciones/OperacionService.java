package pe.edu.upeu.moliselva.operaciones;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
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

    public List<Map<String,Object>> pedidos() { return list("SELECT * FROM pedido ORDER BY id_pedido DESC LIMIT 500"); }
    public Map<String,Object> pedido(long id) { return find("PEDIDO","SELECT * FROM pedido WHERE id_pedido=?",id); }
    private void pedidoPendiente(long id) {
        if (!"PENDIENTE".equals(pedido(id).get("estado"))) {
            throw locked("Solo puede modificar un pedido pendiente");
        }
    }
    @Transactional
    public Map<String,Object> crearPedido(OperacionDto.Pedido d, long userId) {
        active("cliente","id_cliente",d.idCliente());
        var row=write("INSERT INTO pedido(id_cliente,observacion) VALUES (?,?) RETURNING *",
            d.idCliente(),optional(d.observacion()));
        log(userId,"CREAR_PEDIDO",row.get("idPedido")); return row;
    }
    @Transactional
    public Map<String,Object> actualizarPedido(long id, OperacionDto.Pedido d, long userId) {
        pedidoPendiente(id); active("cliente","id_cliente",d.idCliente());
        var row=write("UPDATE pedido SET id_cliente=?,observacion=? WHERE id_pedido=? RETURNING *",
            d.idCliente(),optional(d.observacion()),id);
        log(userId,"ACTUALIZAR_PEDIDO",id); return row;
    }
    @Transactional
    public void eliminarPedido(long id, long userId) {
        pedidoPendiente(id);
        if (exists("SELECT 1 FROM despacho WHERE id_pedido=?",id)) {
            throw locked("El pedido ya tiene un despacho asociado");
        }
        db.update("DELETE FROM pedido WHERE id_pedido=?",id);
        log(userId,"ELIMINAR_PEDIDO",id);
    }
    public List<Map<String,Object>> pedidoDetalles(long id) {
        pedido(id); return list("SELECT * FROM pedido_detalle WHERE id_pedido=? ORDER BY id_producto",id);
    }
    public Map<String,Object> pedidoDetalle(long id, long productoId) {
        return find("PEDIDO_DETALLE","SELECT * FROM pedido_detalle WHERE id_pedido=? AND id_producto=?",id,productoId);
    }
    @Transactional
    public Map<String,Object> crearPedidoDetalle(long id, OperacionDto.PedidoDetalle d, long userId) {
        pedidoPendiente(id); active("producto","id_producto",d.idProducto());
        var row=write("INSERT INTO pedido_detalle(id_pedido,id_producto,cantidad_sacos,cantidad_kg) " +
            "VALUES (?,?,?,?) RETURNING *",id,d.idProducto(),d.cantidadSacos(),d.cantidadKg());
        log(userId,"CREAR_PEDIDO_DETALLE",id+"/"+d.idProducto()); return row;
    }
    @Transactional
    public Map<String,Object> actualizarPedidoDetalle(long id, long productoId,
            OperacionDto.PedidoDetalle d, long userId) {
        require(d.idProducto()==productoId,"DATOS_INVALIDOS","El idProducto del cuerpo debe coincidir con la ruta");
        pedidoPendiente(id); pedidoDetalle(id,productoId);
        var row=write("UPDATE pedido_detalle SET cantidad_sacos=?,cantidad_kg=? " +
            "WHERE id_pedido=? AND id_producto=? RETURNING *",d.cantidadSacos(),d.cantidadKg(),id,productoId);
        log(userId,"ACTUALIZAR_PEDIDO_DETALLE",id+"/"+productoId); return row;
    }
    @Transactional
    public void eliminarPedidoDetalle(long id, long productoId, long userId) {
        pedidoPendiente(id); pedidoDetalle(id,productoId);
        db.update("DELETE FROM pedido_detalle WHERE id_pedido=? AND id_producto=?",id,productoId);
        log(userId,"ELIMINAR_PEDIDO_DETALLE",id+"/"+productoId);
    }

    @Transactional
    public Map<String,Object> cambiarEstadoPedido(long id, OperacionDto.EstadoPedido d, long userId) {
        var row=pedido(id);
        String estado=(String)row.get("estado");
        if ("EN_PREPARACION".equals(d.estado())) {
            if (!"PENDIENTE".equals(estado) ||
                    !exists("SELECT 1 FROM pedido_detalle WHERE id_pedido=?",id)) {
                throw locked("El pedido debe estar pendiente y tener detalles para prepararlo");
            }
        } else if (!List.of("PENDIENTE","EN_PREPARACION").contains(estado) ||
                exists("SELECT 1 FROM despacho WHERE id_pedido=?",id)) {
            throw locked("El pedido no se puede cancelar si tiene despachos");
        }
        var updated=write("UPDATE pedido SET estado=? WHERE id_pedido=? RETURNING *",d.estado(),id);
        log(userId,"CAMBIAR_ESTADO_PEDIDO",id+"/"+d.estado()); return updated;
    }

    public List<Map<String,Object>> despachos() { return list("SELECT * FROM despacho ORDER BY id_despacho DESC LIMIT 500"); }
    public Map<String,Object> despacho(long id) { return find("DESPACHO","SELECT * FROM despacho WHERE id_despacho=?",id); }
    private void despachoBorrador(long id) {
        if (!"BORRADOR".equals(despacho(id).get("estado"))) {
            throw locked("Solo puede modificar un despacho en borrador");
        }
    }
    private void despachoData(OperacionDto.Despacho d) {
        var p=pedido(d.idPedido());
        require(List.of("PENDIENTE","EN_PREPARACION").contains(p.get("estado")),
            "PEDIDO_INVALIDO","El pedido no admite nuevos despachos");
        if ("REPARTO".equals(d.tipoEntrega())) {
            require(d.destino()!=null && !d.destino().isBlank(),"DESTINO_OBLIGATORIO",
                "El reparto requiere destino");
        }
    }
    @Transactional
    public Map<String,Object> crearDespacho(OperacionDto.Despacho d, long userId) {
        despachoData(d);
        var row=write("INSERT INTO despacho(id_pedido,tipo_entrega,destino,id_usuario) " +
            "VALUES (?,?,?,?) RETURNING *",d.idPedido(),d.tipoEntrega(),optional(d.destino()),userId);
        log(userId,"CREAR_DESPACHO",row.get("idDespacho")); return row;
    }
    @Transactional
    public Map<String,Object> actualizarDespacho(long id, OperacionDto.Despacho d, long userId) {
        despachoBorrador(id); despachoData(d);
        var row=write("UPDATE despacho SET id_pedido=?,tipo_entrega=?,destino=? " +
            "WHERE id_despacho=? RETURNING *",d.idPedido(),d.tipoEntrega(),optional(d.destino()),id);
        log(userId,"ACTUALIZAR_DESPACHO",id); return row;
    }
    @Transactional
    public void eliminarDespacho(long id, long userId) {
        despachoBorrador(id);
        if (exists("SELECT 1 FROM movimiento_inventario WHERE id_despacho=?",id)) {
            throw locked("El despacho ya tiene movimientos de inventario");
        }
        db.update("DELETE FROM despacho WHERE id_despacho=?",id);
        log(userId,"ELIMINAR_DESPACHO",id);
    }
    public List<Map<String,Object>> despachoDetalles(long id) {
        despacho(id); return list("SELECT * FROM despacho_detalle WHERE id_despacho=? ORDER BY id_producto",id);
    }
    public Map<String,Object> despachoDetalle(long id, long productoId) {
        return find("DESPACHO_DETALLE","SELECT * FROM despacho_detalle WHERE id_despacho=? AND id_producto=?",id,productoId);
    }
    @Transactional
    public Map<String,Object> crearDespachoDetalle(long id, OperacionDto.DespachoDetalle d, long userId) {
        despachoBorrador(id); active("producto","id_producto",d.idProducto());
        var row=write("INSERT INTO despacho_detalle(id_despacho,id_producto,cantidad_sacos,cantidad_kg) " +
            "VALUES (?,?,?,?) RETURNING *",id,d.idProducto(),d.cantidadSacos(),d.cantidadKg());
        log(userId,"CREAR_DESPACHO_DETALLE",id+"/"+d.idProducto()); return row;
    }
    @Transactional
    public Map<String,Object> actualizarDespachoDetalle(long id, long productoId,
            OperacionDto.DespachoDetalle d, long userId) {
        require(d.idProducto()==productoId,"DATOS_INVALIDOS","El idProducto del cuerpo debe coincidir con la ruta");
        despachoBorrador(id); despachoDetalle(id,productoId);
        var row=write("UPDATE despacho_detalle SET cantidad_sacos=?,cantidad_kg=? " +
            "WHERE id_despacho=? AND id_producto=? RETURNING *",d.cantidadSacos(),d.cantidadKg(),id,productoId);
        log(userId,"ACTUALIZAR_DESPACHO_DETALLE",id+"/"+productoId); return row;
    }
    @Transactional
    public void eliminarDespachoDetalle(long id, long productoId, long userId) {
        despachoBorrador(id); despachoDetalle(id,productoId);
        db.update("DELETE FROM despacho_detalle WHERE id_despacho=? AND id_producto=?",id,productoId);
        log(userId,"ELIMINAR_DESPACHO_DETALLE",id+"/"+productoId);
    }
    @Transactional
    public Map<String,Object> confirmarDespacho(long id, long userId) {
        despachoBorrador(id);
        var row=despacho(id);
        var order=pedido(((Number)row.get("idPedido")).longValue());
        if (!List.of("PENDIENTE","EN_PREPARACION").contains(order.get("estado"))) {
            throw locked("El pedido ya no admite un despacho");
        }
        if (exists("SELECT 1 FROM despacho_detalle dd LEFT JOIN pedido_detalle pd " +
                "ON pd.id_pedido=? AND pd.id_producto=dd.id_producto " +
                "WHERE dd.id_despacho=? AND (pd.id_producto IS NULL " +
                "OR dd.cantidad_sacos>pd.cantidad_sacos OR dd.cantidad_kg>pd.cantidad_kg)",
                row.get("idPedido"),id)) {
            throw locked("El despacho excede los productos o cantidades del pedido");
        }
        try {
            db.confirmarDespacho(id,userId);
        } catch (DataAccessException ex) {
            throw new ApiException(HttpStatus.CONFLICT,"DESPACHO_NO_CONFIRMADO",
                "No se pudo confirmar: compruebe el detalle y el stock disponible");
        }
        log(userId,"CONFIRMAR_DESPACHO",id);
        return despacho(id);
    }

    private String optional(String value) {
        return value==null || value.isBlank() ? null : value.trim();
    }
}
