package pe.edu.upeu.moliselva.proveedor;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores")
@SecurityRequirement(name = "bearerAuth")
public class ProveedorController {
    private final ProveedorService service;
    public ProveedorController(ProveedorService service) { this.service=service; }
    @GetMapping
    @Operation(summary = "Listar proveedores activos")
    public List<ProveedorDto> list() { return service.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar proveedor activo")
    public ProveedorDto find(@PathVariable long id) { return service.find(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary = "Crear proveedor", description = "ADMINISTRADOR o JEFE_PLANTA.")
    public ProveedorDto crear(@Valid @RequestBody ProveedorDto.Datos input,
            @AuthenticationPrincipal Jwt jwt) {
        return service.crear(input,userId(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary = "Actualizar proveedor", description = "ADMINISTRADOR o JEFE_PLANTA.")
    public ProveedorDto actualizar(@PathVariable long id, @Valid @RequestBody ProveedorDto.Datos input,
            @AuthenticationPrincipal Jwt jwt) {
        return service.actualizar(id,input,userId(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary = "Desactivar proveedor", description = "Borrado lógico para conservar las recepciones históricas. ADMINISTRADOR o JEFE_PLANTA.")
    public void eliminar(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminar(id,userId(jwt));
    }

    private long userId(Jwt jwt) { return ((Number)jwt.getClaim("uid")).longValue(); }
}
