package pe.edu.upeu.moliselva.recepcion;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RecepcionRepository {
    private final JdbcTemplate db;
    public RecepcionRepository(JdbcTemplate db) { this.db=db; }

    public record Bloqueo(long idInsumo, BigDecimal pesoNetoKg,
            BigDecimal mermaLimpiezaKg, String estado) {}

    private static final String DETALLE = "SELECT r.id_recepcion,r.fecha_hora,p.razon_social AS proveedor," +
        "r.peso_bruto_kg,r.tara_kg,r.peso_sacos_kg,r.peso_neto_kg,r.humedad_pct," +
        "r.merma_limpieza_kg,r.estado,m.cantidad AS entrada_kardex_kg," +
        "ua.usuario AS autorizado_por " +
        "FROM recepcion r JOIN proveedor p ON p.id_proveedor=r.id_proveedor " +
        "LEFT JOIN usuario ua ON ua.id_usuario=r.id_autoriza " +
        "LEFT JOIN movimiento_inventario m ON m.id_recepcion=r.id_recepcion AND m.tipo_movimiento='ENTRADA' ";

    private static final RowMapper<RecepcionDto.Respuesta> MAPPER = (rs,row) ->
        new RecepcionDto.Respuesta(rs.getLong("id_recepcion"),
            rs.getTimestamp("fecha_hora").toLocalDateTime(),rs.getString("proveedor"),
            rs.getBigDecimal("peso_bruto_kg"),rs.getBigDecimal("tara_kg"),
            rs.getBigDecimal("peso_sacos_kg"),rs.getBigDecimal("peso_neto_kg"),
            rs.getBigDecimal("humedad_pct"),rs.getBigDecimal("merma_limpieza_kg"),
            rs.getString("estado"),rs.getBigDecimal("entrada_kardex_kg"),null,
            rs.getString("autorizado_por"));

    public long insert(long idProveedor, long idInsumo, RecepcionDto.Crear input, String estado) {
        Long id = db.queryForObject("INSERT INTO recepcion(id_proveedor,id_insumo,peso_bruto_kg,tara_kg," +
            "peso_sacos_kg,merma_limpieza_kg,humedad_pct,estado) VALUES (?,?,?,?,?,?,?,?) RETURNING id_recepcion",
            Long.class,idProveedor,idInsumo,input.pesoBrutoKg(),input.taraKg(),input.pesoSacosKg(),
            input.mermaLimpiezaKg(),input.humedadPct(),estado);
        return id;
    }

    public Optional<RecepcionDto.Respuesta> find(long id) {
        return db.query(DETALLE+"WHERE r.id_recepcion=?",MAPPER,id).stream().findFirst();
    }

    public List<RecepcionDto.Respuesta> list(LocalDate date) {
        Timestamp start = Timestamp.valueOf(date.atStartOfDay());
        Timestamp end = Timestamp.valueOf(date.plusDays(1).atStartOfDay());
        return db.query(DETALLE+"WHERE r.fecha_hora>=? AND r.fecha_hora<? ORDER BY r.fecha_hora,r.id_recepcion",
            MAPPER,start,end);
    }

    public Optional<Bloqueo> lock(long id) {
        return db.query("SELECT id_insumo,peso_neto_kg,merma_limpieza_kg,estado " +
            "FROM recepcion WHERE id_recepcion=? FOR UPDATE",
            (rs,row) -> new Bloqueo(rs.getLong("id_insumo"),rs.getBigDecimal("peso_neto_kg"),
                rs.getBigDecimal("merma_limpieza_kg"),rs.getString("estado")),id).stream().findFirst();
    }

    public boolean decide(long id, String status, long userId) {
        return db.update("UPDATE recepcion SET estado=?,id_autoriza=? WHERE id_recepcion=? AND estado='PENDIENTE'",
            status,userId,id) == 1;
    }
}
