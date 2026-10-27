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

@RestController @RequestMapping("/api/formulas")
@Tag(name="Fórmulas y detalles",description="Solo JEFE_PLANTA; las proporciones son confidenciales.") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasRole('JEFE_PLANTA')")
public class FormulaController {
    private final OperacionService service;
    public FormulaController(OperacionService service) { this.service=service; }
    private long uid(Jwt jwt) { return ((Number)jwt.getClaim("uid")).longValue(); }

    @GetMapping @Operation(summary="Listar fórmulas",description="Solo JEFE_PLANTA; las proporciones son confidenciales.")
    public List<Map<String,Object>> list() { return service.formulas(); }
    @GetMapping("/{id}") @Operation(summary="Consultar fórmula")
    public Map<String,Object> find(@PathVariable long id) { return service.formula(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Operation(summary="Crear fórmula")
    public Map<String,Object> create(@Valid @RequestBody OperacionDto.Formula input,
            @AuthenticationPrincipal Jwt jwt) { return service.crearFormula(input,uid(jwt)); }
    @PutMapping("/{id}") @Operation(summary="Actualizar fórmula sin lotes")
    public Map<String,Object> update(@PathVariable long id, @Valid @RequestBody OperacionDto.Formula input,
            @AuthenticationPrincipal Jwt jwt) { return service.actualizarFormula(id,input,uid(jwt)); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Eliminar fórmula sin lotes")
    public void delete(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminarFormula(id,uid(jwt));
    }
    @PatchMapping("/{id}/vigencia") @Operation(summary="Activar o retirar vigencia de fórmula")
    public Map<String,Object> vigencia(@PathVariable long id,
            @Valid @RequestBody OperacionDto.VigenciaFormula input, @AuthenticationPrincipal Jwt jwt) {
        return service.vigenciaFormula(id,input,uid(jwt));
    }

    @GetMapping("/{id}/detalles") @Operation(summary="Listar ingredientes de fórmula")
    public List<Map<String,Object>> details(@PathVariable long id) { return service.formulaDetalles(id); }
    @GetMapping("/{id}/detalles/{insumoId}") @Operation(summary="Consultar ingrediente de fórmula")
    public Map<String,Object> detail(@PathVariable long id, @PathVariable long insumoId) {
        return service.formulaDetalle(id,insumoId);
    }
    @PostMapping("/{id}/detalles") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary="Agregar ingrediente a fórmula")
    public Map<String,Object> createDetail(@PathVariable long id,
            @Valid @RequestBody OperacionDto.FormulaDetalle input, @AuthenticationPrincipal Jwt jwt) {
        return service.crearFormulaDetalle(id,input,uid(jwt));
    }
    @PutMapping("/{id}/detalles/{insumoId}") @Operation(summary="Actualizar ingrediente de fórmula")
    public Map<String,Object> updateDetail(@PathVariable long id, @PathVariable long insumoId,
            @Valid @RequestBody OperacionDto.FormulaDetalle input, @AuthenticationPrincipal Jwt jwt) {
        return service.actualizarFormulaDetalle(id,insumoId,input,uid(jwt));
    }
    @DeleteMapping("/{id}/detalles/{insumoId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Eliminar ingrediente de fórmula")
    public void deleteDetail(@PathVariable long id, @PathVariable long insumoId,
            @AuthenticationPrincipal Jwt jwt) { service.eliminarFormulaDetalle(id,insumoId,uid(jwt)); }
}
