package pe.edu.upeu.moliselva.presentation.rest.formulas;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.formulas.usecase.ConsultarFormulasUseCase;
import pe.edu.upeu.moliselva.application.formulas.usecase.GestionarDetallesFormulaUseCase;
import pe.edu.upeu.moliselva.application.formulas.usecase.GestionarFormulasUseCase;
import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.presentation.rest.formulas.dto.OperacionDto;
import pe.edu.upeu.moliselva.presentation.rest.formulas.mapper.FormulaView;

@RestController @RequestMapping("/api/formulas")
@Tag(name="Fórmulas y detalles",description="Solo JEFE_PLANTA; las proporciones son confidenciales.") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasRole('JEFE_PLANTA')")
public class FormulaController {
    private final ConsultarFormulasUseCase consultar;
    private final GestionarFormulasUseCase gestionar;
    private final GestionarDetallesFormulaUseCase detalles;
    private final ActorActual actor;

    public FormulaController(ConsultarFormulasUseCase consultar, GestionarFormulasUseCase gestionar,
            GestionarDetallesFormulaUseCase detalles, ActorActual actor) {
        this.consultar=consultar; this.gestionar=gestionar; this.detalles=detalles; this.actor=actor;
    }

    private long uid() { return actor.obtener().id(); }

    @GetMapping @Operation(summary="Listar fórmulas",description="Solo JEFE_PLANTA; las proporciones son confidenciales.")
    public List<Map<String,Object>> list() { return consultar.listar().stream().map(FormulaView::from).toList(); }
    @GetMapping("/{id}") @Operation(summary="Consultar fórmula")
    public Map<String,Object> find(@PathVariable long id) { return FormulaView.from(consultar.obtener(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Operation(summary="Crear fórmula")
    public Map<String,Object> create(@Valid @RequestBody OperacionDto.Formula input) {
        return FormulaView.from(gestionar.crear(FormulaView.aDatos(input),uid()));
    }
    @PutMapping("/{id}") @Operation(summary="Actualizar fórmula sin lotes")
    public Map<String,Object> update(@PathVariable long id, @Valid @RequestBody OperacionDto.Formula input) {
        return FormulaView.from(gestionar.actualizar(id,FormulaView.aDatos(input),uid()));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Eliminar fórmula sin lotes")
    public void delete(@PathVariable long id) { gestionar.eliminar(id,uid()); }
    @PatchMapping("/{id}/vigencia") @Operation(summary="Activar o retirar vigencia de fórmula")
    public Map<String,Object> vigencia(@PathVariable long id,
            @Valid @RequestBody OperacionDto.VigenciaFormula input) {
        return FormulaView.from(gestionar.cambiarVigencia(id,input.vigente(),uid()));
    }
    @GetMapping("/{id}/detalles") @Operation(summary="Listar ingredientes de fórmula")
    public List<Map<String,Object>> details(@PathVariable long id) {
        return consultar.listarDetalles(id).stream().map(FormulaView::from).toList();
    }
    @GetMapping("/{id}/detalles/{insumoId}") @Operation(summary="Consultar ingrediente de fórmula")
    public Map<String,Object> detail(@PathVariable long id, @PathVariable long insumoId) {
        return FormulaView.from(consultar.obtenerDetalle(id,insumoId));
    }
    @PostMapping("/{id}/detalles") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary="Agregar ingrediente a fórmula")
    public Map<String,Object> createDetail(@PathVariable long id,
            @Valid @RequestBody OperacionDto.FormulaDetalle input) {
        return FormulaView.from(detalles.agregar(id,input.idInsumo(),input.proporcionConfidencial(),uid()));
    }
    @PutMapping("/{id}/detalles/{insumoId}") @Operation(summary="Actualizar ingrediente de fórmula")
    public Map<String,Object> updateDetail(@PathVariable long id, @PathVariable long insumoId,
            @Valid @RequestBody OperacionDto.FormulaDetalle input) {
        return FormulaView.from(detalles.actualizar(id,insumoId,input.idInsumo(),input.proporcionConfidencial(),uid()));
    }
    @DeleteMapping("/{id}/detalles/{insumoId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary="Eliminar ingrediente de fórmula")
    public void deleteDetail(@PathVariable long id, @PathVariable long insumoId) {
        detalles.eliminar(id,insumoId,uid());
    }
}
