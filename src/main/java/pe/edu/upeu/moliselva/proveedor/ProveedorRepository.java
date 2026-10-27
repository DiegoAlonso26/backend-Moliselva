package pe.edu.upeu.moliselva.proveedor;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProveedorRepository {
    private final JdbcTemplate db;
    public ProveedorRepository(JdbcTemplate db) { this.db=db; }

    public List<ProveedorDto> activos() {
        return db.query("SELECT id_proveedor,razon_social FROM proveedor WHERE activo=true ORDER BY razon_social",
            (rs,row) -> new ProveedorDto(rs.getLong("id_proveedor"),rs.getString("razon_social")));
    }

    public boolean existeActivo(long id) {
        Integer count = db.queryForObject("SELECT count(*) FROM proveedor WHERE id_proveedor=? AND activo=true",
            Integer.class,id);
        return count != null && count == 1;
    }
}
