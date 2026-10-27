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

@RestController @RequestMapping("/api/pedidos")
@Tag(name="Pedidos y detalles",description="ADMINISTRADOR, JEFE_PLANTA y CONTADORA.") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
public class PedidoController {
    private final OperacionService service;
    public PedidoController(OperacionService service) { this.service=service; }
    private long uid(Jwt jwt) { return ((Number)jwt.getClaim("uid")).longValue(); }

    @GetMapping @Operation(summary="Listar pedidos")
    public List<Map<String,Object>> list() { return service.pedidos(); }
    @GetMapping("/{id}") @Operation(summary="Consultar pedido")
    public Map<String,Object> find(@PathVariable long id) { return service.pedido(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Crear pedido")
    public Map<String,Object> create(@Valid @RequestBody OperacionDto.Pedido input,
            @AuthenticationPrincipal Jwt jwt) { return service.crearPedido(input,uid(jwt)); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Actualizar pedido pendiente")
    public Map<String,Object> update(@PathVariable long id, @Valid @RequestBody OperacionDto.Pedido input,
            @AuthenticationPrincipal Jwt jwt) { return service.actualizarPedido(id,input,uid(jwt)); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Eliminar pedido pendiente sin despacho")
    public void delete(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminarPedido(id,uid(jwt));
    }
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Preparar o cancelar pedido")
    public Map<String,Object> estado(@PathVariable long id,
            @Valid @RequestBody OperacionDto.EstadoPedido input, @AuthenticationPrincipal Jwt jwt) {
        return service.cambiarEstadoPedido(id,input,uid(jwt));
    }

    @GetMapping("/{id}/detalles") @Operation(summary="Listar productos del pedido")
    public List<Map<String,Object>> details(@PathVariable long id) { return service.pedidoDetalles(id); }
    @GetMapping("/{id}/detalles/{productoId}") @Operation(summary="Consultar producto del pedido")
    public Map<String,Object> detail(@PathVariable long id, @PathVariable long productoId) {
        return service.pedidoDetalle(id,productoId);
    }
    @PostMapping("/{id}/detalles") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Agregar producto al pedido pendiente")
    public Map<String,Object> createDetail(@PathVariable long id,
            @Valid @RequestBody OperacionDto.PedidoDetalle input, @AuthenticationPrincipal Jwt jwt) {
        return service.crearPedidoDetalle(id,input,uid(jwt));
    }
    @PutMapping("/{id}/detalles/{productoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Actualizar producto del pedido pendiente")
    public Map<String,Object> updateDetail(@PathVariable long id, @PathVariable long productoId,
            @Valid @RequestBody OperacionDto.PedidoDetalle input, @AuthenticationPrincipal Jwt jwt) {
        return service.actualizarPedidoDetalle(id,productoId,input,uid(jwt));
    }
    @DeleteMapping("/{id}/detalles/{productoId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Eliminar producto del pedido pendiente")
    public void deleteDetail(@PathVariable long id, @PathVariable long productoId,
            @AuthenticationPrincipal Jwt jwt) { service.eliminarPedidoDetalle(id,productoId,uid(jwt)); }
}
