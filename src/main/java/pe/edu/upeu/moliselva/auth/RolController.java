package pe.edu.upeu.moliselva.auth;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.comun.ApiException;

@RestController @RequestMapping("/api/roles")
@Tag(name="Roles del sistema") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class RolController {
    public record Rol(short idRol, String nombre, String descripcion) {}
    private final JdbcTemplate db;
    public RolController(JdbcTemplate db) { this.db=db; }
    @GetMapping @Operation(summary="Listar roles configurados")
    public List<Rol> list() {
        return db.query("SELECT id_rol,nombre,descripcion FROM rol ORDER BY id_rol",
            (rs,n) -> new Rol(rs.getShort("id_rol"),rs.getString("nombre"),rs.getString("descripcion")));
    }
    @GetMapping("/{id}") @Operation(summary="Consultar rol")
    public Rol find(@PathVariable short id) {
        return db.query("SELECT id_rol,nombre,descripcion FROM rol WHERE id_rol=?",
            (rs,n) -> new Rol(rs.getShort("id_rol"),rs.getString("nombre"),rs.getString("descripcion")),id)
            .stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND,"ROL_NO_ENCONTRADO","El rol no existe"));
    }
}
