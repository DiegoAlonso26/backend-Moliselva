package pe.edu.upeu.moliselva.auth;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Autenticacion y usuarios")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service=service; }

    @PostMapping("/auth/login")
    @Operation(summary = "Iniciar sesion y obtener un token JWT")
    public AuthDto.LoginResponse login(@Valid @RequestBody AuthDto.LoginRequest input) {
        return service.login(input);
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Consultar el usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public AuthDto.Usuario me(@AuthenticationPrincipal Jwt jwt) { return service.current(jwt); }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear usuario", description = "Solo ADMINISTRADOR.", security = @SecurityRequirement(name = "bearerAuth"))
    public AuthDto.Usuario create(@Valid @RequestBody AuthDto.NuevoUsuario input,
            @AuthenticationPrincipal Jwt jwt) {
        return service.create(input,((Number)jwt.getClaim("uid")).longValue());
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuarios", description = "Solo ADMINISTRADOR.", security = @SecurityRequirement(name = "bearerAuth"))
    public List<AuthDto.Usuario> list() { return service.list(); }
}
