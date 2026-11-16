package pe.edu.upeu.moliselva.presentation.rest.recepcion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.recepcion.usecase.ConsultarRecepcionUseCase;
import pe.edu.upeu.moliselva.application.recepcion.usecase.CorregirRecepcionUseCase;
import pe.edu.upeu.moliselva.application.recepcion.usecase.DecidirRecepcionUseCase;
import pe.edu.upeu.moliselva.application.recepcion.usecase.EliminarRecepcionUseCase;
import pe.edu.upeu.moliselva.application.recepcion.usecase.RegistrarRecepcionUseCase;
import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.presentation.rest.recepcion.dto.RecepcionDto;
import pe.edu.upeu.moliselva.presentation.rest.recepcion.mapper.RecepcionMapper;

@RestController
@RequestMapping("/api/recepciones")
@Tag(name = "Recepciones de maiz")
@SecurityRequirement(name = "bearerAuth")
public class RecepcionController {
    private final RegistrarRecepcionUseCase registrar;
    private final DecidirRecepcionUseCase decidir;
    private final CorregirRecepcionUseCase corregir;
    private final EliminarRecepcionUseCase eliminar;
    private final ConsultarRecepcionUseCase consultar;
    private final ActorActual actor;

    public RecepcionController(RegistrarRecepcionUseCase registrar,
            DecidirRecepcionUseCase decidir, CorregirRecepcionUseCase corregir,
            EliminarRecepcionUseCase eliminar, ConsultarRecepcionUseCase consultar,
            ActorActual actor) {
        this.registrar=registrar; this.decidir=decidir; this.corregir=corregir;
        this.eliminar=eliminar; this.consultar=consultar; this.actor=actor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('OPERARIO','JEFE_PLANTA')")
    @Operation(summary = "Registrar recepcion", description = "OPERARIO o JEFE_PLANTA. Si humedad es hasta 14.5%, se acepta; entre 14.5% y 15%, queda pendiente; por encima de 15%, se rechaza.")
    public RecepcionDto.Respuesta crear(@Valid @RequestBody RecepcionDto.Crear input) {
        return RecepcionMapper.aRespuesta(registrar.registrar(RecepcionMapper.aDatos(input),actor.obtener()));
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary = "Decidir recepcion pendiente", description = "Solo JEFE_PLANTA.")
    public RecepcionDto.Respuesta decidir(@PathVariable long id,
            @Valid @RequestBody RecepcionDto.Decision input) {
        return RecepcionMapper.aRespuesta(decidir.decidir(id,RecepcionMapper.aDecision(input),
            input.motivo(),actor.obtener()));
    }

    @GetMapping
    @Operation(summary = "Listar recepciones", description = "Sin fecha, usa la fecha actual de Lima.")
    public List<RecepcionDto.Respuesta> listar(
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return consultar.listar(fecha==null ? LocalDate.now(ZoneId.of("America/Lima")) : fecha)
            .stream().map(RecepcionMapper::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar recepción")
    public RecepcionDto.Respuesta find(@PathVariable long id) {
        return RecepcionMapper.aRespuesta(consultar.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERARIO','JEFE_PLANTA')")
    @Operation(summary = "Corregir recepción no decidida", description = "Solo una recepción PENDIENTE o RECHAZADA automáticamente, sin decisión del jefe. Si la corrección la acepta, crea la entrada al kardex.")
    public RecepcionDto.Respuesta actualizar(@PathVariable long id, @Valid @RequestBody RecepcionDto.Crear input) {
        return RecepcionMapper.aRespuesta(corregir.corregir(id,RecepcionMapper.aDatos(input),actor.obtener()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('OPERARIO','JEFE_PLANTA')")
    @Operation(summary = "Eliminar recepción no decidida", description = "Solo una recepción PENDIENTE o RECHAZADA automáticamente, sin entrada al kardex ni decisión del jefe.")
    public void eliminar(@PathVariable long id) {
        eliminar.eliminar(id,actor.obtener());
    }
}
