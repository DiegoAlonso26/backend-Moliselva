package pe.edu.upeu.moliselva.recepcion.adapter.out.jdbc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.recepcion.application.port.out.RecepcionPort;
import pe.edu.upeu.moliselva.recepcion.domain.EstadoRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionBloqueo;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionDatos;

@Repository
public class JdbcRecepcionAdapter implements RecepcionPort {
    private final JdbcTemplate db;
    public JdbcRecepcionAdapter(JdbcTemplate db) { this.db=db; }

    private static final String DETALLE = "SELECT r.id_recepcion,r.fecha_hora,r.id_proveedor,p.razon_social AS proveedor," +
        "r.peso_bruto_kg,r.tara_kg,r.peso_sacos_kg,r.peso_neto_kg,r.humedad_pct," +
        "r.merma_limpieza_kg,r.estado,m.cantidad AS entrada_kardex_kg," +
        "ua.usuario AS autorizado_por " +
        "FROM recepcion r JOIN proveedor p ON p.id_proveedor=r.id_proveedor " +
        "LEFT JOIN usuario ua ON ua.id_usuario=r.id_autoriza " +
        "LEFT JOIN movimiento_inventario m ON m.id_recepcion=r.id_recepcion AND m.tipo_movimiento='ENTRADA' ";

    private static final RowMapper<Recepcion> MAPPER = (rs,row) ->
        new Recepcion(rs.getLong("id_recepcion"),
            rs.getTimestamp("fecha_hora").toLocalDateTime(),rs.getLong("id_proveedor"),rs.getString("proveedor"),
            rs.getBigDecimal("peso_bruto_kg"),rs.getBigDecimal("tara_kg"),
            rs.getBigDecimal("peso_sacos_kg"),rs.getBigDecimal("peso_neto_kg"),
            rs.getBigDecimal("humedad_pct"),rs.getBigDecimal("merma_limpieza_kg"),
            EstadoRecepcion.valueOf(rs.getString("estado")),rs.getBigDecimal("entrada_kardex_kg"),null,
            rs.getString("autorizado_por"));

    @Override
    public long insertar(RecepcionDatos datos, long idInsumo, EstadoRecepcion estado) {
        Long id = db.queryForObject("INSERT INTO recepcion(id_proveedor,id_insumo,peso_bruto_kg,tara_kg," +
            "peso_sacos_kg,merma_limpieza_kg,humedad_pct,estado) VALUES (?,?,?,?,?,?,?,?) RETURNING id_recepcion",
            Long.class,datos.idProveedor(),idInsumo,datos.pesoBrutoKg(),datos.taraKg(),datos.pesoSacosKg(),
            datos.mermaLimpiezaKg(),datos.humedadPct(),estado.name());
        return id;
    }

    @Override
    public Optional<Recepcion> buscar(long id) {
        return db.query(DETALLE+"WHERE r.id_recepcion=?",MAPPER,id).stream().findFirst();
    }

    @Override
    public List<Recepcion> listar(LocalDate date) {
        Timestamp start = Timestamp.valueOf(date.atStartOfDay());
        Timestamp end = Timestamp.valueOf(date.plusDays(1).atStartOfDay());
        return db.query(DETALLE+"WHERE r.fecha_hora>=? AND r.fecha_hora<? ORDER BY r.fecha_hora,r.id_recepcion",
            MAPPER,start,end);
    }

    @Override
    public Optional<RecepcionBloqueo> bloquear(long id) {
        return db.query("SELECT id_insumo,peso_neto_kg,merma_limpieza_kg,estado,id_autoriza " +
            "FROM recepcion WHERE id_recepcion=? FOR UPDATE",
            (rs,row) -> new RecepcionBloqueo(rs.getLong("id_insumo"),rs.getBigDecimal("peso_neto_kg"),
                rs.getBigDecimal("merma_limpieza_kg"),EstadoRecepcion.valueOf(rs.getString("estado")),
                rs.getObject("id_autoriza",Long.class)),id).stream().findFirst();
    }

    @Override
    public boolean decidir(long id, EstadoRecepcion estado, long userId) {
        return db.update("UPDATE recepcion SET estado=?,id_autoriza=? WHERE id_recepcion=? AND estado='PENDIENTE'",
            estado.name(),userId,id) == 1;
    }

    @Override
    public boolean corregir(long id, RecepcionDatos datos, EstadoRecepcion estado) {
        return db.update("UPDATE recepcion SET id_proveedor=?,peso_bruto_kg=?,tara_kg=?," +
            "peso_sacos_kg=?,merma_limpieza_kg=?,humedad_pct=?,estado=? " +
            "WHERE id_recepcion=? AND estado IN ('PENDIENTE','RECHAZADA') AND id_autoriza IS NULL",
            datos.idProveedor(),datos.pesoBrutoKg(),datos.taraKg(),datos.pesoSacosKg(),
            datos.mermaLimpiezaKg(),datos.humedadPct(),estado.name(),id) == 1;
    }

    @Override
    public boolean eliminarEditable(long id) {
        return db.update("DELETE FROM recepcion WHERE id_recepcion=? " +
            "AND estado IN ('PENDIENTE','RECHAZADA') AND id_autoriza IS NULL " +
            "AND NOT EXISTS (SELECT 1 FROM movimiento_inventario WHERE id_recepcion=?)",id,id) == 1;
    }
}
