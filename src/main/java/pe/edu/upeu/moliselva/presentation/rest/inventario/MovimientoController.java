package pe.edu.upeu.moliselva.presentation.rest.inventario;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.inventario.usecase.ConsultarInventarioUseCase;
import pe.edu.upeu.moliselva.application.inventario.usecase.RegistrarAjusteUseCase;
import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.presentation.rest.inventario.dto.InventarioDto;
import pe.edu.upeu.moliselva.presentation.rest.inventario.mapper.InventarioMapper;

@RestController
@RequestMapping("/api/inventario")
@Tag(name="Movimientos de inventario")
@SecurityRequirement(name="bearerAuth")
public class MovimientoController {
    private final ConsultarInventarioUseCase consultar;
    private final RegistrarAjusteUseCase ajustes;
    private final ActorActual actor;

    public MovimientoController(ConsultarInventarioUseCase consultar, RegistrarAjusteUseCase ajustes, ActorActual actor) {
        this.consultar=consultar; this.ajustes=ajustes; this.actor=actor;
    }

    @GetMapping("/stock") @Operation(summary="Consultar stock de todos los insumos y productos")
    public List<InventarioDto.ItemStock> stock() {
        return consultar.stockTodos().stream().map(InventarioMapper::aRespuesta).toList();
    }
    @GetMapping("/stock/{tipo}/{id}") @Operation(summary="Consultar stock de un insumo o producto")
    public InventarioDto.ItemStock stockItem(@PathVariable String tipo, @PathVariable long id) {
        return InventarioMapper.aRespuesta(consultar.stockItem(tipo,id));
    }
    @GetMapping("/movimientos") @Operation(summary="Listar los últimos 500 movimientos")
    public List<InventarioDto.Movimiento> list() {
        return consultar.movimientos().stream().map(InventarioMapper::aRespuesta).toList();
    }
    @GetMapping("/movimientos/{id}") @Operation(summary="Consultar movimiento")
    public InventarioDto.Movimiento find(@PathVariable long id) {
        return InventarioMapper.aRespuesta(consultar.movimiento(id));
    }
    @PostMapping("/movimientos/ajustes") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Registrar ajuste compensatorio", description="El kardex es inmutable: se corrige con AJUSTE_ENTRADA o AJUSTE_SALIDA, no editando movimientos históricos.")
    public InventarioDto.Movimiento ajustar(@Valid @RequestBody InventarioDto.Ajuste input) {
        return InventarioMapper.aRespuesta(ajustes.ajustar(InventarioMapper.aAjuste(input),actor.obtener().id()));
    }
}
