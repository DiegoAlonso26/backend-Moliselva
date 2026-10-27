package pe.edu.upeu.moliselva.inventario;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventario")
@Tag(name="Movimientos de inventario")
@SecurityRequirement(name="bearerAuth")
public class MovimientoController {
    private final InventarioService service;
    public MovimientoController(InventarioService service) { this.service=service; }
    @GetMapping("/stock") @Operation(summary="Consultar stock de todos los insumos y productos")
    public List<InventarioDto.ItemStock> stock() { return service.stockTodos(); }
    @GetMapping("/stock/{tipo}/{id}") @Operation(summary="Consultar stock de un insumo o producto")
    public InventarioDto.ItemStock stockItem(@PathVariable String tipo, @PathVariable long id) {
        return service.stockItem(tipo,id);
    }
    @GetMapping("/movimientos") @Operation(summary="Listar los últimos 500 movimientos")
    public List<InventarioDto.Movimiento> list() { return service.movimientos(); }
    @GetMapping("/movimientos/{id}") @Operation(summary="Consultar movimiento")
    public InventarioDto.Movimiento find(@PathVariable long id) { return service.movimiento(id); }
    @PostMapping("/movimientos/ajustes") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Registrar ajuste compensatorio", description="El kardex es inmutable: se corrige con AJUSTE_ENTRADA o AJUSTE_SALIDA, no editando movimientos históricos.")
    public InventarioDto.Movimiento ajustar(@Valid @RequestBody InventarioDto.Ajuste input,
            @AuthenticationPrincipal Jwt jwt) {
        return service.ajustar(input,((Number)jwt.getClaim("uid")).longValue());
    }
}
