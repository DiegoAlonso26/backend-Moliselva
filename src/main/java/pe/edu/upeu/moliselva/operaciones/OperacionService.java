package pe.edu.upeu.moliselva.operaciones;

import java.sql.Timestamp;
import java.math.BigDecimal;
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

    public List<Map<String,Object>> lotes() { return list("SELECT * FROM lote ORDER BY id_lote DESC LIMIT 500"); }
    public Map<String,Object> lote(long id) { return find("LOTE","SELECT * FROM lote WHERE id_lote=?",id); }
    private void loteData(OperacionDto.Lote d) {
        require(d.tamanoKg()==500 || d.tamanoKg()==1000,"TAMANO_INVALIDO","El lote debe ser de 500 o 1000 kg");
        active("producto","id_producto",d.idProducto());
        require(exists("SELECT 1 FROM formula WHERE id_formula=? AND id_producto=? AND vigente=true",
            d.idFormula(),d.idProducto()),"FORMULA_INVALIDA","La fórmula no corresponde al producto o no está vigente");
        require(exists("SELECT 1 FROM formula_detalle WHERE id_formula=?",d.idFormula()),
            "FORMULA_SIN_DETALLE","La fórmula debe tener al menos un ingrediente");
    }
    private void lotePlanificado(long id) {
        var row=lote(id);
        if (!"PLANIFICADO".equals(row.get("estado")) ||
                exists("SELECT 1 FROM movimiento_inventario WHERE id_lote=?",id)) {
            throw locked("Solo puede editar un lote planificado sin movimientos");
        }
    }
    private void loteInsumoEditable(long id) {
        var row=lote(id);
        if (!List.of("PLANIFICADO","EN_PROCESO").contains(row.get("estado")) ||
                exists("SELECT 1 FROM movimiento_inventario WHERE id_lote=?",id)) {
            throw locked("Los insumos del lote ya no se pueden modificar");
        }
    }
    @Transactional
    public Map<String,Object> crearLote(OperacionDto.Lote d, long userId) {
        loteData(d);
        var row=write("INSERT INTO lote(id_producto,id_formula,tamano_kg,id_responsable) " +
            "VALUES (?,?,?,?) RETURNING *",d.idProducto(),d.idFormula(),d.tamanoKg(),userId);
        log(userId,"CREAR_LOTE",row.get("idLote")); return row;
    }
    @Transactional
    public Map<String,Object> actualizarLote(long id, OperacionDto.Lote d, long userId) {
        lotePlanificado(id); loteData(d);
        var row=write("UPDATE lote SET id_producto=?,id_formula=?,tamano_kg=? " +
            "WHERE id_lote=? RETURNING *",d.idProducto(),d.idFormula(),d.tamanoKg(),id);
        log(userId,"ACTUALIZAR_LOTE",id); return row;
    }
    @Transactional
    public void eliminarLote(long id, long userId) {
        lotePlanificado(id);
        db.update("DELETE FROM lote WHERE id_lote=?",id);
        log(userId,"ELIMINAR_LOTE",id);
    }
    public List<Map<String,Object>> loteInsumos(long id) {
        lote(id); return list("SELECT * FROM lote_insumo WHERE id_lote=? ORDER BY id_insumo",id);
    }
    public Map<String,Object> loteInsumo(long id, long insumoId) {
        return find("LOTE_INSUMO","SELECT * FROM lote_insumo WHERE id_lote=? AND id_insumo=?",id,insumoId);
    }
    @Transactional
    public Map<String,Object> crearLoteInsumo(long id, OperacionDto.LoteInsumo d, long userId) {
        lotePlanificado(id); active("insumo","id_insumo",d.idInsumo());
        require(exists("SELECT 1 FROM lote l JOIN formula_detalle fd ON fd.id_formula=l.id_formula " +
            "WHERE l.id_lote=? AND fd.id_insumo=?",id,d.idInsumo()),
            "INSUMO_FUERA_DE_FORMULA","El insumo no pertenece a la fórmula del lote");
        var row=write("INSERT INTO lote_insumo(id_lote,id_insumo,cantidad_calculada_kg,cantidad_real_kg) " +
            "VALUES (?,?,?,?) RETURNING *",id,d.idInsumo(),d.cantidadCalculadaKg(),d.cantidadRealKg());
        log(userId,"CREAR_LOTE_INSUMO",id+"/"+d.idInsumo()); return row;
    }
    @Transactional
    public Map<String,Object> actualizarLoteInsumo(long id, long insumoId,
            OperacionDto.LoteInsumo d, long userId) {
        require(d.idInsumo()==insumoId,"DATOS_INVALIDOS","El idInsumo del cuerpo debe coincidir con la ruta");
        loteInsumoEditable(id); loteInsumo(id,insumoId);
        var row=write("UPDATE lote_insumo SET cantidad_calculada_kg=?,cantidad_real_kg=? " +
            "WHERE id_lote=? AND id_insumo=? RETURNING *",d.cantidadCalculadaKg(),d.cantidadRealKg(),id,insumoId);
        log(userId,"ACTUALIZAR_LOTE_INSUMO",id+"/"+insumoId); return row;
    }
    @Transactional
    public void eliminarLoteInsumo(long id, long insumoId, long userId) {
        lotePlanificado(id); loteInsumo(id,insumoId);
        db.update("DELETE FROM lote_insumo WHERE id_lote=? AND id_insumo=?",id,insumoId);
        log(userId,"ELIMINAR_LOTE_INSUMO",id+"/"+insumoId);
    }

    @Transactional
    public Map<String,Object> cambiarEstadoLote(long id, OperacionDto.EstadoLote d, long userId) {
        var row=lote(id);
        String estado=(String)row.get("estado");
        if ("EN_PROCESO".equals(d.estado())) {
            if (!"PLANIFICADO".equals(estado) ||
                    !exists("SELECT 1 FROM lote_insumo WHERE id_lote=?",id)) {
                throw locked("El lote debe estar planificado y tener insumos para iniciar");
            }
        } else if (!List.of("PLANIFICADO","EN_PROCESO").contains(estado) ||
                exists("SELECT 1 FROM movimiento_inventario WHERE id_lote=?",id)) {
            throw locked("El lote no se puede anular después de registrar movimientos");
        }
        var updated=write("UPDATE lote SET estado=?,id_responsable=? WHERE id_lote=? RETURNING *",
            d.estado(),userId,id);
        log(userId,"CAMBIAR_ESTADO_LOTE",id+"/"+d.estado()); return updated;
    }

    @Transactional
    public Map<String,Object> finalizarLote(long id, long userId) {
        var row=db.one("SELECT * FROM lote WHERE id_lote=? FOR UPDATE",id).orElseThrow(() -> missing("LOTE"));
        if (!"EN_PROCESO".equals(row.get("estado"))) {
            throw locked("Solo puede finalizar un lote en proceso");
        }
        var details=db.list("SELECT * FROM lote_insumo WHERE id_lote=? ORDER BY id_insumo",id);
        if (details.isEmpty()) { throw locked("El lote no tiene insumos"); }
        if (exists("SELECT 1 FROM formula_detalle fd LEFT JOIN lote_insumo li " +
                "ON li.id_lote=? AND li.id_insumo=fd.id_insumo " +
                "WHERE fd.id_formula=? AND li.id_insumo IS NULL",id,row.get("id_formula"))) {
            throw locked("Faltan insumos de la fórmula en el lote");
        }
        try {
            for (var detail : details) {
                Object real=detail.get("cantidad_real_kg");
                BigDecimal cantidad=(BigDecimal)(real==null ? detail.get("cantidad_calculada_kg") : real);
                long insumoId=((Number)detail.get("id_insumo")).longValue();
                active("insumo","id_insumo",insumoId);
                db.insert("INSERT INTO movimiento_inventario(tipo_movimiento,id_insumo,cantidad," +
                    "unidad_medida,id_lote,id_usuario) SELECT 'SALIDA',?, ?,unidad_medida,?,? " +
                    "FROM insumo WHERE id_insumo=? RETURNING id_movimiento",
                    insumoId,cantidad,id,userId,insumoId);
            }
            db.insert("INSERT INTO movimiento_inventario(tipo_movimiento,id_producto,cantidad," +
                "unidad_medida,id_lote,id_usuario) VALUES ('ENTRADA',?,?,'kg',?,?) RETURNING id_movimiento",
                row.get("id_producto"),row.get("tamano_kg"),id,userId);
        } catch (DataAccessException ex) {
            if (ex.getMessage()!=null && ex.getMessage().contains("Stock insuficiente")) {
                throw new ApiException(HttpStatus.CONFLICT,"STOCK_INSUFICIENTE","No hay insumos suficientes para finalizar el lote");
            }
            throw ex;
        }
        db.update("UPDATE lote SET estado='TERMINADO',id_responsable=? WHERE id_lote=?",userId,id);
        log(userId,"FINALIZAR_LOTE",id);
        return lote(id);
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
