package pe.edu.upeu.moliselva.presentation.rest.auth;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.auth.usecase.ConsultarUsuariosUseCase;
import pe.edu.upeu.moliselva.application.auth.usecase.GestionarUsuariosUseCase;
import pe.edu.upeu.moliselva.application.auth.usecase.IniciarSesionUseCase;
import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.presentation.rest.auth.dto.AuthDto;
import pe.edu.upeu.moliselva.presentation.rest.auth.mapper.AuthMapper;

@RestController
@RequestMapping("/api")
@Tag(name = "Autenticacion y usuarios")
public class AuthController {
    private final IniciarSesionUseCase iniciarSesion;
    private final ConsultarUsuariosUseCase consultar;
    private final GestionarUsuariosUseCase gestionar;
    private final ActorActual actor;

    public AuthController(IniciarSesionUseCase iniciarSesion, ConsultarUsuariosUseCase consultar,
            GestionarUsuariosUseCase gestionar, ActorActual actor) {
        this.iniciarSesion=iniciarSesion; this.consultar=consultar; this.gestionar=gestionar; this.actor=actor;
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Iniciar sesion y obtener un token JWT")
    public AuthDto.LoginResponse login(@Valid @RequestBody AuthDto.LoginRequest input) {
        return AuthMapper.aRespuesta(iniciarSesion.iniciar(input.usuario(),input.password()));
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Consultar el usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public AuthDto.Usuario me() { return AuthMapper.aRespuesta(consultar.actual(uid())); }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear usuario", description = "Solo ADMINISTRADOR.", security = @SecurityRequirement(name = "bearerAuth"))
    public AuthDto.Usuario create(@Valid @RequestBody AuthDto.NuevoUsuario input) {
        return AuthMapper.aRespuesta(gestionar.crear(AuthMapper.aDatos(input),input.password(),uid()));
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuarios", description = "Solo ADMINISTRADOR.", security = @SecurityRequirement(name = "bearerAuth"))
    public List<AuthDto.Usuario> list() {
        return consultar.listar().stream().map(AuthMapper::aRespuesta).toList();
    }

    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Consultar usuario", description = "Solo ADMINISTRADOR.", security = @SecurityRequirement(name = "bearerAuth"))
    public AuthDto.Usuario find(@PathVariable long id) { return AuthMapper.aRespuesta(consultar.obtener(id)); }

    @PutMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar usuario", description = "Solo ADMINISTRADOR. Omitir password para conservar la clave.", security = @SecurityRequirement(name = "bearerAuth"))
    public AuthDto.Usuario update(@PathVariable long id, @Valid @RequestBody AuthDto.ActualizarUsuario input) {
        return AuthMapper.aRespuesta(gestionar.actualizar(id,AuthMapper.aDatos(input),input.password(),uid()));
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desactivar usuario", description = "Solo ADMINISTRADOR. Borrado lógico; no puede desactivarse a sí mismo.", security = @SecurityRequirement(name = "bearerAuth"))
    public void delete(@PathVariable long id) { gestionar.desactivar(id,uid()); }

    private long uid() { return actor.obtener().id(); }
}
