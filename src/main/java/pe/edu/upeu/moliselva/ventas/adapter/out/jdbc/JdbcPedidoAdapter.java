package pe.edu.upeu.moliselva.ventas.adapter.out.jdbc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.operaciones.OperacionRepository;
import pe.edu.upeu.moliselva.ventas.application.port.out.PedidoPort;
import pe.edu.upeu.moliselva.ventas.domain.DatosDetalle;
import pe.edu.upeu.moliselva.ventas.domain.DatosPedido;
import pe.edu.upeu.moliselva.ventas.domain.EstadoPedido;
import pe.edu.upeu.moliselva.ventas.domain.Pedido;
import pe.edu.upeu.moliselva.ventas.domain.PedidoDetalle;

@Repository
public class JdbcPedidoAdapter implements PedidoPort {
    private final OperacionRepository db;
    public JdbcPedidoAdapter(OperacionRepository db) { this.db=db; }
    private static Pedido pedido(Map<String,Object> row) {
        return new Pedido(((Number)row.get("id_pedido")).longValue(),
            ((Number)row.get("id_cliente")).longValue(),
            ((Timestamp)row.get("fecha_hora")).toLocalDateTime(),
            EstadoPedido.valueOf((String)row.get("estado")),(String)row.get("observacion"));
    }
    private static PedidoDetalle detalle(Map<String,Object> row) {
        return new PedidoDetalle(((Number)row.get("id_pedido")).longValue(),
            ((Number)row.get("id_producto")).longValue(),
            ((Number)row.get("cantidad_sacos")).intValue(),(BigDecimal)row.get("cantidad_kg"));
    }
    @Override public List<Pedido> listar() {
        return db.list("SELECT * FROM pedido ORDER BY id_pedido DESC LIMIT 500").stream()
            .map(JdbcPedidoAdapter::pedido).toList();
    }
    @Override public Optional<Pedido> buscar(long id) {
        return db.one("SELECT * FROM pedido WHERE id_pedido=?",id).map(JdbcPedidoAdapter::pedido);
    }
    @Override public Optional<Pedido> bloquear(long id) {
        return db.one("SELECT * FROM pedido WHERE id_pedido=? FOR UPDATE",id)
            .map(JdbcPedidoAdapter::pedido);
    }
    @Override public Pedido crear(DatosPedido datos) {
        return pedido(db.insert("INSERT INTO pedido(id_cliente,observacion) VALUES (?,?) RETURNING *",
            datos.idCliente(),datos.observacionNormalizada()));
    }
    @Override public Pedido actualizar(long id, DatosPedido datos) {
        return pedido(db.insert("UPDATE pedido SET id_cliente=?,observacion=? WHERE id_pedido=? RETURNING *",
            datos.idCliente(),datos.observacionNormalizada(),id));
    }
    @Override public void eliminar(long id) { db.update("DELETE FROM pedido WHERE id_pedido=?",id); }
    @Override public Pedido cambiarEstado(long id, EstadoPedido estado) {
        return pedido(db.insert("UPDATE pedido SET estado=? WHERE id_pedido=? RETURNING *",estado.name(),id));
    }
    @Override public boolean tieneDespachos(long id) {
        return db.one("SELECT 1 FROM despacho WHERE id_pedido=?",id).isPresent();
    }
    @Override public boolean tieneDetalles(long id) {
        return db.one("SELECT 1 FROM pedido_detalle WHERE id_pedido=?",id).isPresent();
    }
    @Override public List<PedidoDetalle> listarDetalles(long id) {
        return db.list("SELECT * FROM pedido_detalle WHERE id_pedido=? ORDER BY id_producto",id)
            .stream().map(JdbcPedidoAdapter::detalle).toList();
    }
    @Override public Optional<PedidoDetalle> buscarDetalle(long id, long productoId) {
        return db.one("SELECT * FROM pedido_detalle WHERE id_pedido=? AND id_producto=?",id,productoId)
            .map(JdbcPedidoAdapter::detalle);
    }
    @Override public PedidoDetalle agregarDetalle(long id, DatosDetalle datos) {
        return detalle(db.insert("INSERT INTO pedido_detalle(id_pedido,id_producto,cantidad_sacos,cantidad_kg) " +
            "VALUES (?,?,?,?) RETURNING *",id,datos.idProducto(),datos.cantidadSacos(),datos.cantidadKg()));
    }
    @Override public PedidoDetalle actualizarDetalle(long id, long productoId, DatosDetalle datos) {
        return detalle(db.insert("UPDATE pedido_detalle SET cantidad_sacos=?,cantidad_kg=? " +
            "WHERE id_pedido=? AND id_producto=? RETURNING *",
            datos.cantidadSacos(),datos.cantidadKg(),id,productoId));
    }
    @Override public void eliminarDetalle(long id, long productoId) {
        db.update("DELETE FROM pedido_detalle WHERE id_pedido=? AND id_producto=?",id,productoId);
    }
}
