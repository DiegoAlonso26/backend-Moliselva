package pe.edu.upeu.moliselva.presentation.rest.auth;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.auth.usecase.ConsultarRolesUseCase;
import pe.edu.upeu.moliselva.presentation.rest.auth.dto.Rol;
import pe.edu.upeu.moliselva.presentation.rest.auth.mapper.AuthMapper;

@RestController @RequestMapping("/api/roles")
@Tag(name="Roles del sistema") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class RolController {
    private final ConsultarRolesUseCase roles;

    public RolController(ConsultarRolesUseCase roles) { this.roles=roles; }

    @GetMapping @Operation(summary="Listar roles configurados")
    public List<Rol> list() { return roles.listar().stream().map(AuthMapper::aRespuesta).toList(); }

    @GetMapping("/{id}") @Operation(summary="Consultar rol")
    public Rol find(@PathVariable short id) { return AuthMapper.aRespuesta(roles.obtener(id)); }
}
