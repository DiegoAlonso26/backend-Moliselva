package pe.edu.upeu.moliselva.presentation.rest.produccion;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.presentation.rest.produccion.dto.LoteDto;
import pe.edu.upeu.moliselva.presentation.rest.produccion.mapper.LoteView;
import pe.edu.upeu.moliselva.application.produccion.usecase.CambiarEstadoLoteUseCase;
import pe.edu.upeu.moliselva.application.produccion.usecase.ConsultarLotesUseCase;
import pe.edu.upeu.moliselva.application.produccion.usecase.FinalizarLoteUseCase;
import pe.edu.upeu.moliselva.application.produccion.usecase.GestionarInsumosLoteUseCase;
import pe.edu.upeu.moliselva.application.produccion.usecase.PlanificarLoteUseCase;

@RestController @RequestMapping("/api/lotes")
@Tag(name="Lotes y sus insumos",description="Lectura JEFE_PLANTA y OPERARIO; escritura solo JEFE_PLANTA.") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasAnyRole('JEFE_PLANTA','OPERARIO')")
public class LoteController {
    private final ConsultarLotesUseCase consultar;
    private final PlanificarLoteUseCase planificar;
    private final GestionarInsumosLoteUseCase insumos;
    private final CambiarEstadoLoteUseCase cambiarEstado;
    private final FinalizarLoteUseCase finalizar;
    private final ActorActual actor;
    public LoteController(ConsultarLotesUseCase consultar, PlanificarLoteUseCase planificar,
            GestionarInsumosLoteUseCase insumos, CambiarEstadoLoteUseCase cambiarEstado,
            FinalizarLoteUseCase finalizar, ActorActual actor) {
        this.actor=actor;
        this.consultar=consultar; this.planificar=planificar; this.insumos=insumos;
        this.cambiarEstado=cambiarEstado; this.finalizar=finalizar;
    }
    private long uid() { return actor.obtener().id(); }

    @GetMapping @Operation(summary="Listar lotes")
    public List<Map<String,Object>> list() {
        return consultar.listar().stream().map(LoteView::from).toList();
    }
    @GetMapping("/{id}") @Operation(summary="Consultar lote")
    public Map<String,Object> find(@PathVariable long id) { return LoteView.from(consultar.obtener(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Planificar lote")
    public Map<String,Object> create(@Valid @RequestBody LoteDto.Lote input) {
        return LoteView.from(planificar.crear(input.toDatos(),uid()));
    }
    @PutMapping("/{id}") @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Actualizar lote planificado")
    public Map<String,Object> update(@PathVariable long id, @Valid @RequestBody LoteDto.Lote input) {
        return LoteView.from(planificar.actualizar(id,input.toDatos(),uid()));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JEFE_PLANTA')") @Operation(summary="Eliminar lote planificado")
    public void delete(@PathVariable long id) {
        planificar.eliminar(id,uid());
    }
    @PatchMapping("/{id}/estado") @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Iniciar o anular lote")
    public Map<String,Object> estado(@PathVariable long id,
            @Valid @RequestBody LoteDto.EstadoLote input) {
        return LoteView.from(cambiarEstado.cambiar(id,input.toEstado(),uid()));
    }
    @PostMapping("/{id}/finalizar") @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Finalizar lote",description="Descuenta insumos y registra producto terminado en el kardex en una transacción.")
    public Map<String,Object> finalizar(@PathVariable long id) {
        return LoteView.from(finalizar.finalizar(id,uid()));
    }
    @GetMapping("/{id}/insumos") @Operation(summary="Listar insumos de lote")
    @PreAuthorize("hasRole('JEFE_PLANTA')")
    public List<Map<String,Object>> details(@PathVariable long id) {
        return consultar.listarInsumos(id).stream().map(LoteView::from).toList();
    }
    @GetMapping("/{id}/insumos/{insumoId}") @Operation(summary="Consultar insumo de lote")
    @PreAuthorize("hasRole('JEFE_PLANTA')")
    public Map<String,Object> detail(@PathVariable long id, @PathVariable long insumoId) {
        return LoteView.from(consultar.obtenerInsumo(id,insumoId));
    }
    @PostMapping("/{id}/insumos") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('JEFE_PLANTA')") @Operation(summary="Agregar insumo a lote planificado")
    public Map<String,Object> createDetail(@PathVariable long id,
            @Valid @RequestBody LoteDto.LoteInsumo input) {
        return LoteView.from(insumos.agregar(id,input.toDatos(),uid()));
    }
    @PutMapping("/{id}/insumos/{insumoId}") @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary="Actualizar insumo de lote planificado")
    public Map<String,Object> updateDetail(@PathVariable long id, @PathVariable long insumoId,
            @Valid @RequestBody LoteDto.LoteInsumo input) {
        return LoteView.from(insumos.actualizar(id,insumoId,input.toDatos(),uid()));
    }
    @DeleteMapping("/{id}/insumos/{insumoId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JEFE_PLANTA')") @Operation(summary="Eliminar insumo de lote planificado")
    public void deleteDetail(@PathVariable long id, @PathVariable long insumoId) {
        insumos.eliminar(id,insumoId,uid());
    }
}
