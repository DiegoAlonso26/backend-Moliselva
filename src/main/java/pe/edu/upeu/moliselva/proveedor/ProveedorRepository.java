package pe.edu.upeu.moliselva.proveedor;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ProveedorRepository {
    private static final RowMapper<ProveedorDto> MAPPER = (rs,row) -> new ProveedorDto(
        rs.getLong("id_proveedor"),rs.getString("razon_social"),
        rs.getString("documento"),rs.getString("telefono"));
    private final JdbcTemplate db;
    public ProveedorRepository(JdbcTemplate db) { this.db=db; }

    public List<ProveedorDto> activos() {
        return db.query("SELECT id_proveedor,razon_social,documento,telefono " +
            "FROM proveedor WHERE activo=true ORDER BY razon_social,id_proveedor",MAPPER);
    }

    public Optional<ProveedorDto> find(long id) {
        return db.query("SELECT id_proveedor,razon_social,documento,telefono " +
            "FROM proveedor WHERE id_proveedor=? AND activo=true",MAPPER,id).stream().findFirst();
    }

    public ProveedorDto crear(String razonSocial, String documento, String telefono) {
        return db.query("INSERT INTO proveedor(razon_social,documento,telefono) VALUES (?,?,?) " +
            "RETURNING id_proveedor,razon_social,documento,telefono",MAPPER,
            razonSocial,documento,telefono).get(0);
    }

    public Optional<ProveedorDto> actualizar(long id, String razonSocial, String documento, String telefono) {
        return db.query("UPDATE proveedor SET razon_social=?,documento=?,telefono=? " +
            "WHERE id_proveedor=? AND activo=true " +
            "RETURNING id_proveedor,razon_social,documento,telefono",MAPPER,
            razonSocial,documento,telefono,id).stream().findFirst();
    }

    public boolean desactivar(long id) {
        return db.update("UPDATE proveedor SET activo=false WHERE id_proveedor=? AND activo=true",id) == 1;
    }

    public boolean existeActivo(long id) {
        Integer count = db.queryForObject("SELECT count(*) FROM proveedor WHERE id_proveedor=? AND activo=true",
            Integer.class,id);
        return count != null && count == 1;
    }
}
