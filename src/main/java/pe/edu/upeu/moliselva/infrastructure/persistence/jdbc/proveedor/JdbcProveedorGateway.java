package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.proveedor;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.proveedor.gateway.ProveedorGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;
import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;
import pe.edu.upeu.moliselva.domain.proveedor.ProveedorDatos;

@Repository
public class JdbcProveedorGateway implements ProveedorGateway, ProveedorActivoGateway {
    private static final String COLUMNAS = "id_proveedor,razon_social,documento,telefono";
    private static final RowMapper<Proveedor> MAPPER = (rs,row) -> new Proveedor(
        rs.getLong("id_proveedor"),rs.getString("razon_social"),
        rs.getString("documento"),rs.getString("telefono"));
    private final JdbcTemplate db;

    public JdbcProveedorGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public List<Proveedor> activos() {
        return db.query("SELECT "+COLUMNAS+" FROM proveedor WHERE activo=true ORDER BY razon_social,id_proveedor",MAPPER);
    }

    @Override
    public Optional<Proveedor> buscarActivo(long id) {
        return db.query("SELECT "+COLUMNAS+" FROM proveedor WHERE id_proveedor=? AND activo=true",MAPPER,id)
            .stream().findFirst();
    }

    @Override
    public Proveedor crear(ProveedorDatos datos) {
        return db.query("INSERT INTO proveedor(razon_social,documento,telefono) VALUES (?,?,?) RETURNING "+COLUMNAS,
            MAPPER,datos.razonSocial(),datos.documento(),datos.telefono()).get(0);
    }

    @Override
    public Optional<Proveedor> actualizar(long id, ProveedorDatos datos) {
        return db.query("UPDATE proveedor SET razon_social=?,documento=?,telefono=? " +
            "WHERE id_proveedor=? AND activo=true RETURNING "+COLUMNAS,MAPPER,
            datos.razonSocial(),datos.documento(),datos.telefono(),id).stream().findFirst();
    }

    @Override
    public boolean desactivar(long id) {
        return db.update("UPDATE proveedor SET activo=false WHERE id_proveedor=? AND activo=true",id) == 1;
    }

    @Override
    public boolean existeActivo(long idProveedor) {
        Integer count = db.queryForObject("SELECT count(*) FROM proveedor WHERE id_proveedor=? AND activo=true",
            Integer.class,idProveedor);
        return count != null && count == 1;
    }
}
