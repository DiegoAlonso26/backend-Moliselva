package pe.edu.upeu.moliselva.recepcion.adapter.in.web;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.recepcion.application.ActorRecepcion;
import pe.edu.upeu.moliselva.recepcion.application.port.in.ConsultarRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.in.CorregirRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.in.DecidirRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.in.EliminarRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.in.RegistrarRecepcionUseCase;

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
    public RecepcionController(RegistrarRecepcionUseCase registrar,
            DecidirRecepcionUseCase decidir, CorregirRecepcionUseCase corregir,
            EliminarRecepcionUseCase eliminar, ConsultarRecepcionUseCase consultar) {
        this.registrar=registrar; this.decidir=decidir; this.corregir=corregir;
        this.eliminar=eliminar; this.consultar=consultar;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('OPERARIO','JEFE_PLANTA')")
    @Operation(summary = "Registrar recepcion", description = "OPERARIO o JEFE_PLANTA. Si humedad es hasta 14.5%, se acepta; entre 14.5% y 15%, queda pendiente; por encima de 15%, se rechaza.")
    public RecepcionDto.Respuesta crear(@Valid @RequestBody RecepcionDto.Crear input,
            Authentication authentication) {
        return RecepcionDto.Respuesta.from(registrar.registrar(input.toDatos(),actor(authentication)));
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('JEFE_PLANTA')")
    @Operation(summary = "Decidir recepcion pendiente", description = "Solo JEFE_PLANTA.")
    public RecepcionDto.Respuesta decidir(@PathVariable long id,
            @Valid @RequestBody RecepcionDto.Decision input, Authentication authentication) {
        return RecepcionDto.Respuesta.from(decidir.decidir(id,input.toDecision(),
            input.motivo(),actor(authentication)));
    }

    @GetMapping
    @Operation(summary = "Listar recepciones", description = "Sin fecha, usa la fecha actual de Lima.")
    public List<RecepcionDto.Respuesta> listar(
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return consultar.listar(fecha==null ? LocalDate.now(ZoneId.of("America/Lima")) : fecha)
            .stream().map(RecepcionDto.Respuesta::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar recepción")
    public RecepcionDto.Respuesta find(@PathVariable long id) {
        return RecepcionDto.Respuesta.from(consultar.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERARIO','JEFE_PLANTA')")
    @Operation(summary = "Corregir recepción no decidida", description = "Solo una recepción PENDIENTE o RECHAZADA automáticamente, sin decisión del jefe. Si la corrección la acepta, crea la entrada al kardex.")
    public RecepcionDto.Respuesta actualizar(@PathVariable long id, @Valid @RequestBody RecepcionDto.Crear input,
            Authentication authentication) {
        return RecepcionDto.Respuesta.from(corregir.corregir(id,input.toDatos(),actor(authentication)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('OPERARIO','JEFE_PLANTA')")
    @Operation(summary = "Eliminar recepción no decidida", description = "Solo una recepción PENDIENTE o RECHAZADA automáticamente, sin entrada al kardex ni decisión del jefe.")
    public void eliminar(@PathVariable long id, Authentication authentication) {
        eliminar.eliminar(id,actor(authentication));
    }

    private ActorRecepcion actor(Authentication authentication) {
        Jwt jwt=(Jwt)authentication.getPrincipal();
        String rol=authentication.getAuthorities().stream().map(a -> a.getAuthority())
            .filter(a -> a.startsWith("ROLE_")).findFirst()
            .map(a -> a.substring("ROLE_".length()))
            .orElseThrow(() -> new IllegalStateException("Rol autenticado ausente"));
        return new ActorRecepcion(((Number)jwt.getClaim("uid")).longValue(),jwt.getSubject(),rol);
    }
}
