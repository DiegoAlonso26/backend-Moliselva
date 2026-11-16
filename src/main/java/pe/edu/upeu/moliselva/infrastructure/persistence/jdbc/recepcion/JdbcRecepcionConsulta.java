package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.recepcion;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionConsultaGateway;
import pe.edu.upeu.moliselva.domain.recepcion.EstadoRecepcion;

/**
 * Lado de lectura de la recepción: SQL con los JOIN a proveedor, usuarios y kardex,
 * mapeado directo a {@link RecepcionResponse} (como Dapper), sin pasar por la entidad.
 */
@Repository
public class JdbcRecepcionConsulta implements RecepcionConsultaGateway {
    private final JdbcTemplate db;
    public JdbcRecepcionConsulta(JdbcTemplate db) { this.db=db; }

    private static final String DETALLE = "SELECT r.id_recepcion,r.fecha_hora,r.id_proveedor,p.razon_social AS proveedor," +
        "r.peso_bruto_kg,r.tara_kg,r.peso_sacos_kg,r.peso_neto_kg,r.humedad_pct," +
        "r.merma_limpieza_kg,r.estado,m.cantidad AS entrada_kardex_kg," +
        "ur.usuario AS registrado_por,ua.usuario AS autorizado_por " +
        "FROM recepcion r JOIN proveedor p ON p.id_proveedor=r.id_proveedor " +
        "LEFT JOIN usuario ur ON ur.id_usuario=r.id_registra " +
        "LEFT JOIN usuario ua ON ua.id_usuario=r.id_autoriza " +
        "LEFT JOIN movimiento_inventario m ON m.id_recepcion=r.id_recepcion AND m.tipo_movimiento='ENTRADA' ";

    private static final RowMapper<RecepcionResponse> MAPPER = (rs,row) ->
        new RecepcionResponse(rs.getLong("id_recepcion"),
            rs.getTimestamp("fecha_hora").toLocalDateTime(),rs.getLong("id_proveedor"),rs.getString("proveedor"),
            rs.getBigDecimal("peso_bruto_kg"),rs.getBigDecimal("tara_kg"),
            rs.getBigDecimal("peso_sacos_kg"),rs.getBigDecimal("peso_neto_kg"),
            rs.getBigDecimal("humedad_pct"),rs.getBigDecimal("merma_limpieza_kg"),
            EstadoRecepcion.valueOf(rs.getString("estado")),rs.getBigDecimal("entrada_kardex_kg"),rs.getString("registrado_por"),
            rs.getString("autorizado_por"));

    @Override
    public Optional<RecepcionResponse> buscar(long id) {
        return db.query(DETALLE+"WHERE r.id_recepcion=?",MAPPER,id).stream().findFirst();
    }

    @Override
    public List<RecepcionResponse> listar(LocalDate date) {
        Timestamp start = Timestamp.valueOf(date.atStartOfDay());
        Timestamp end = Timestamp.valueOf(date.plusDays(1).atStartOfDay());
        return db.query(DETALLE+"WHERE r.fecha_hora>=? AND r.fecha_hora<? ORDER BY r.fecha_hora,r.id_recepcion",
            MAPPER,start,end);
    }
}
