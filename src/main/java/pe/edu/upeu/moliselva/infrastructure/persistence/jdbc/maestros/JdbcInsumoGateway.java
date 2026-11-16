package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.maestros;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.maestros.gateway.InsumoGateway;
import pe.edu.upeu.moliselva.domain.maestros.Insumo;
import pe.edu.upeu.moliselva.domain.maestros.InsumoDatos;

@Repository
public class JdbcInsumoGateway implements InsumoGateway {
    private static final RowMapper<Insumo> INSUMO = (rs,n) -> new Insumo(
        rs.getLong("id_insumo"),rs.getString("nombre"),rs.getString("unidad_medida"));
    private static final String COLUMNAS = "id_insumo,nombre,unidad_medida";
    private final JdbcTemplate db;

    public JdbcInsumoGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public List<Insumo> activos() {
        return db.query("SELECT "+COLUMNAS+" FROM insumo WHERE activo=true ORDER BY nombre,id_insumo",INSUMO);
    }

    @Override
    public Optional<Insumo> buscarActivo(long id) {
        return db.query("SELECT "+COLUMNAS+" FROM insumo WHERE id_insumo=? AND activo=true",INSUMO,id).stream().findFirst();
    }

    @Override
    public Insumo crear(InsumoDatos d) {
        return db.query("INSERT INTO insumo(nombre,unidad_medida) VALUES (?,?) RETURNING "+COLUMNAS,
            INSUMO,d.nombre(),d.unidadMedida()).get(0);
    }

    @Override
    public Optional<Insumo> actualizar(long id, InsumoDatos d) {
        return db.query("UPDATE insumo SET nombre=?,unidad_medida=? WHERE id_insumo=? AND activo=true " +
            "RETURNING "+COLUMNAS,INSUMO,d.nombre(),d.unidadMedida(),id).stream().findFirst();
    }

    @Override
    public boolean desactivar(long id) {
        return db.update("UPDATE insumo SET activo=false WHERE id_insumo=? AND activo=true",id)==1;
    }

    @Override
    public boolean enUso(long id) {
        Boolean inUse=db.queryForObject("SELECT " +
            "EXISTS(SELECT 1 FROM formula_detalle fd JOIN formula f ON f.id_formula=fd.id_formula " +
            "WHERE fd.id_insumo=? AND f.vigente=true) OR " +
            "EXISTS(SELECT 1 FROM lote_insumo li JOIN lote l ON l.id_lote=li.id_lote " +
            "WHERE li.id_insumo=? AND l.estado IN ('PLANIFICADO','EN_PROCESO'))",
            Boolean.class,id,id);
        return Boolean.TRUE.equals(inUse);
    }
}
