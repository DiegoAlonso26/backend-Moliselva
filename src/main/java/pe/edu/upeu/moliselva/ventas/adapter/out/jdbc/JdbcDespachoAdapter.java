package pe.edu.upeu.moliselva.ventas.adapter.out.jdbc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.operaciones.OperacionRepository;
import pe.edu.upeu.moliselva.ventas.application.VentaFailure;
import pe.edu.upeu.moliselva.ventas.application.port.out.DespachoPort;
import pe.edu.upeu.moliselva.ventas.domain.DatosDespacho;
import pe.edu.upeu.moliselva.ventas.domain.DatosDetalle;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;
import pe.edu.upeu.moliselva.ventas.domain.DespachoDetalle;
import pe.edu.upeu.moliselva.ventas.domain.EstadoDespacho;
import pe.edu.upeu.moliselva.ventas.domain.TipoEntrega;

@Repository
public class JdbcDespachoAdapter implements DespachoPort {
    private final OperacionRepository db;
    public JdbcDespachoAdapter(OperacionRepository db) { this.db=db; }
    private static Despacho despacho(Map<String,Object> row) {
        return new Despacho(((Number)row.get("id_despacho")).longValue(),
            ((Number)row.get("id_pedido")).longValue(),
            ((Timestamp)row.get("fecha_hora")).toLocalDateTime(),
            TipoEntrega.valueOf((String)row.get("tipo_entrega")),(String)row.get("destino"),
            EstadoDespacho.valueOf((String)row.get("estado")),
            ((Number)row.get("id_usuario")).longValue());
    }
    private static DespachoDetalle detalle(Map<String,Object> row) {
        return new DespachoDetalle(((Number)row.get("id_despacho")).longValue(),
            ((Number)row.get("id_producto")).longValue(),
            ((Number)row.get("cantidad_sacos")).intValue(),(BigDecimal)row.get("cantidad_kg"));
    }
    @Override public List<Despacho> listar() {
        return db.list("SELECT * FROM despacho ORDER BY id_despacho DESC LIMIT 500").stream()
            .map(JdbcDespachoAdapter::despacho).toList();
    }
    @Override public Optional<Despacho> buscar(long id) {
        return db.one("SELECT * FROM despacho WHERE id_despacho=?",id).map(JdbcDespachoAdapter::despacho);
    }
    @Override public Optional<Despacho> bloquear(long id) {
        return db.one("SELECT * FROM despacho WHERE id_despacho=? FOR UPDATE",id)
            .map(JdbcDespachoAdapter::despacho);
    }
    @Override public Despacho crear(DatosDespacho datos, long usuarioId) {
        return despacho(db.insert("INSERT INTO despacho(id_pedido,tipo_entrega,destino,id_usuario) " +
            "VALUES (?,?,?,?) RETURNING *",datos.idPedido(),datos.tipoEntrega().name(),
            datos.destinoNormalizado(),usuarioId));
    }
    @Override public Despacho actualizar(long id, DatosDespacho datos) {
        return despacho(db.insert("UPDATE despacho SET id_pedido=?,tipo_entrega=?,destino=? " +
            "WHERE id_despacho=? RETURNING *",datos.idPedido(),datos.tipoEntrega().name(),
            datos.destinoNormalizado(),id));
    }
    @Override public void eliminar(long id) { db.update("DELETE FROM despacho WHERE id_despacho=?",id); }
    @Override public boolean tieneMovimientos(long id) {
        return db.one("SELECT 1 FROM movimiento_inventario WHERE id_despacho=?",id).isPresent();
    }
    @Override public boolean excedePedido(long idPedido, long idDespacho) {
        return db.one("SELECT 1 FROM despacho_detalle dd LEFT JOIN pedido_detalle pd " +
            "ON pd.id_pedido=? AND pd.id_producto=dd.id_producto " +
            "WHERE dd.id_despacho=? AND (pd.id_producto IS NULL " +
            "OR dd.cantidad_sacos>pd.cantidad_sacos OR dd.cantidad_kg>pd.cantidad_kg)",
            idPedido,idDespacho).isPresent();
    }
    @Override public List<DespachoDetalle> listarDetalles(long id) {
        return db.list("SELECT * FROM despacho_detalle WHERE id_despacho=? ORDER BY id_producto",id)
            .stream().map(JdbcDespachoAdapter::detalle).toList();
    }
    @Override public Optional<DespachoDetalle> buscarDetalle(long id, long productoId) {
        return db.one("SELECT * FROM despacho_detalle WHERE id_despacho=? AND id_producto=?",id,productoId)
            .map(JdbcDespachoAdapter::detalle);
    }
    @Override public DespachoDetalle agregarDetalle(long id, DatosDetalle datos) {
        return detalle(db.insert("INSERT INTO despacho_detalle(id_despacho,id_producto,cantidad_sacos,cantidad_kg) " +
            "VALUES (?,?,?,?) RETURNING *",id,datos.idProducto(),datos.cantidadSacos(),datos.cantidadKg()));
    }
    @Override public DespachoDetalle actualizarDetalle(long id, long productoId, DatosDetalle datos) {
        return detalle(db.insert("UPDATE despacho_detalle SET cantidad_sacos=?,cantidad_kg=? " +
            "WHERE id_despacho=? AND id_producto=? RETURNING *",
            datos.cantidadSacos(),datos.cantidadKg(),id,productoId));
    }
    @Override public void eliminarDetalle(long id, long productoId) {
        db.update("DELETE FROM despacho_detalle WHERE id_despacho=? AND id_producto=?",id,productoId);
    }
    @Override public void confirmarEnBase(long id, long usuarioId) {
        try {
            db.confirmarDespacho(id,usuarioId);
        } catch (DataAccessException ex) {
            throw new VentaFailure(VentaFailure.Type.CONFLICTO,"DESPACHO_NO_CONFIRMADO",
                "No se pudo confirmar: compruebe el detalle y el stock disponible");
        }
    }
}
