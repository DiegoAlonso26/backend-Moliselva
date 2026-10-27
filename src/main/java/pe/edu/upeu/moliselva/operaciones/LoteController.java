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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/api/lotes")
@Tag(name="Lotes y sus insumos",description="Lectura JEFE_PLANTA y OPERARIO; escritura solo JEFE_PLANTA.") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasAnyRole('JEFE_PLANTA','OPERARIO')")
public class LoteController {
    private final OperacionService service;
    public LoteController(OperacionService service) { this.service=service; }
    private long uid(Jwt jwt) { return ((Number)jwt.getClaim("uid")).longValue(); }

    @GetMapping @Operation(summary="Listar lotes")
    public List<Map<String,Object>> list() { return service.lotes(); }
    @GetMapping("/{id}") @Operation(summary="Consultar lote")
    public Map<String,Object> find(@PathVariable long id) { return service.lote(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Planificar lote")
    public Map<String,Object> create(@Valid @RequestBody OperacionDto.Lote input,
            @AuthenticationPrincipal Jwt jwt) { return service.crearLote(input,uid(jwt)); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Actualizar lote planificado")
    public Map<String,Object> update(@PathVariable long id, @Valid @RequestBody OperacionDto.Lote input,
            @AuthenticationPrincipal Jwt jwt) { return service.actualizarLote(id,input,uid(jwt)); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JEFE_PLANTA')") @Operation(summary="Eliminar lote planificado")
    public void delete(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminarLote(id,uid(jwt));
    }

    @PatchMapping("/{id}/estado") @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Iniciar o anular lote")
    public Map<String,Object> estado(@PathVariable long id,
            @Valid @RequestBody OperacionDto.EstadoLote input, @AuthenticationPrincipal Jwt jwt) {
        return service.cambiarEstadoLote(id,input,uid(jwt));
    }
    @PostMapping("/{id}/finalizar") @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Finalizar lote",description="Descuenta insumos y registra producto terminado en el kardex en una transacción.")
    public Map<String,Object> finalizar(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return service.finalizarLote(id,uid(jwt));
    }

    @GetMapping("/{id}/insumos") @Operation(summary="Listar insumos de lote")
    public List<Map<String,Object>> details(@PathVariable long id) { return service.loteInsumos(id); }
    @GetMapping("/{id}/insumos/{insumoId}") @Operation(summary="Consultar insumo de lote")
    public Map<String,Object> detail(@PathVariable long id, @PathVariable long insumoId) {
        return service.loteInsumo(id,insumoId);
    }
    @PostMapping("/{id}/insumos") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('JEFE_PLANTA')") @Operation(summary="Agregar insumo a lote planificado")
    public Map<String,Object> createDetail(@PathVariable long id,
            @Valid @RequestBody OperacionDto.LoteInsumo input, @AuthenticationPrincipal Jwt jwt) {
        return service.crearLoteInsumo(id,input,uid(jwt));
    }
    @PutMapping("/{id}/insumos/{insumoId}") @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Actualizar insumo de lote planificado")
    public Map<String,Object> updateDetail(@PathVariable long id, @PathVariable long insumoId,
            @Valid @RequestBody OperacionDto.LoteInsumo input, @AuthenticationPrincipal Jwt jwt) {
        return service.actualizarLoteInsumo(id,insumoId,input,uid(jwt));
    }
    @DeleteMapping("/{id}/insumos/{insumoId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JEFE_PLANTA')") @Operation(summary="Eliminar insumo de lote planificado")
    public void deleteDetail(@PathVariable long id, @PathVariable long insumoId,
            @AuthenticationPrincipal Jwt jwt) { service.eliminarLoteInsumo(id,insumoId,uid(jwt)); }
}
