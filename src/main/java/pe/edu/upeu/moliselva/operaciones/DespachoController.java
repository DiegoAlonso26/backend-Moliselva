package pe.edu.upeu.moliselva.operaciones;

import java.util.List;
import java.util.Map;
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

@RestController @RequestMapping("/api/despachos")
@Tag(name="Despachos y detalles",description="ADMINISTRADOR, JEFE_PLANTA y CONTADORA.") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
public class DespachoController {
    private final OperacionService service;
    public DespachoController(OperacionService service) { this.service=service; }
    private long uid(Jwt jwt) { return ((Number)jwt.getClaim("uid")).longValue(); }

    @GetMapping @Operation(summary="Listar despachos")
    public List<Map<String,Object>> list() { return service.despachos(); }
    @GetMapping("/{id}") @Operation(summary="Consultar despacho")
    public Map<String,Object> find(@PathVariable long id) { return service.despacho(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Crear despacho en borrador")
    public Map<String,Object> create(@Valid @RequestBody OperacionDto.Despacho input,
            @AuthenticationPrincipal Jwt jwt) { return service.crearDespacho(input,uid(jwt)); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Actualizar despacho en borrador")
    public Map<String,Object> update(@PathVariable long id, @Valid @RequestBody OperacionDto.Despacho input,
            @AuthenticationPrincipal Jwt jwt) { return service.actualizarDespacho(id,input,uid(jwt)); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Eliminar despacho en borrador")
    public void delete(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminarDespacho(id,uid(jwt));
    }

    @GetMapping("/{id}/detalles") @Operation(summary="Listar productos del despacho")
    public List<Map<String,Object>> details(@PathVariable long id) { return service.despachoDetalles(id); }
    @GetMapping("/{id}/detalles/{productoId}") @Operation(summary="Consultar producto del despacho")
    public Map<String,Object> detail(@PathVariable long id, @PathVariable long productoId) {
        return service.despachoDetalle(id,productoId);
    }
    @PostMapping("/{id}/detalles") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Agregar producto al despacho en borrador")
    public Map<String,Object> createDetail(@PathVariable long id,
            @Valid @RequestBody OperacionDto.DespachoDetalle input, @AuthenticationPrincipal Jwt jwt) {
        return service.crearDespachoDetalle(id,input,uid(jwt));
    }
    @PutMapping("/{id}/detalles/{productoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Actualizar producto del despacho en borrador")
    public Map<String,Object> updateDetail(@PathVariable long id, @PathVariable long productoId,
            @Valid @RequestBody OperacionDto.DespachoDetalle input, @AuthenticationPrincipal Jwt jwt) {
        return service.actualizarDespachoDetalle(id,productoId,input,uid(jwt));
    }
    @DeleteMapping("/{id}/detalles/{productoId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Eliminar producto del despacho en borrador")
    public void deleteDetail(@PathVariable long id, @PathVariable long productoId,
            @AuthenticationPrincipal Jwt jwt) { service.eliminarDespachoDetalle(id,productoId,uid(jwt)); }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Confirmar despacho",description="Llama al procedimiento de la BD que valida stock y registra salidas al kardex.")
    public Map<String,Object> confirm(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return service.confirmarDespacho(id,uid(jwt));
    }
}
