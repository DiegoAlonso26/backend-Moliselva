package pe.edu.upeu.moliselva.presentation.rest.ventas;

import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.presentation.rest.ventas.dto.VentaDto;
import pe.edu.upeu.moliselva.presentation.rest.ventas.mapper.VentaView;
import pe.edu.upeu.moliselva.application.ventas.usecase.ConsultarPedidosUseCase;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarPedidosUseCase;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarDetallesPedidoUseCase;

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

@RestController @RequestMapping("/api/pedidos")
@Tag(name="Pedidos y detalles",description="ADMINISTRADOR, JEFE_PLANTA y CONTADORA.") @SecurityRequirement(name="bearerAuth")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
public class PedidoController {
    private final ConsultarPedidosUseCase consultar;
    private final GestionarPedidosUseCase gestionar;
    private final GestionarDetallesPedidoUseCase detalles;
    private final ActorActual actor;
    public PedidoController(ConsultarPedidosUseCase consultar, GestionarPedidosUseCase gestionar,
            GestionarDetallesPedidoUseCase detalles, ActorActual actor) {
        this.actor=actor;
        this.consultar=consultar; this.gestionar=gestionar; this.detalles=detalles;
    }
    private long uid() { return actor.obtener().id(); }

    @GetMapping @Operation(summary="Listar pedidos")
    public List<Map<String,Object>> list() { return consultar.listar().stream().map(VentaView::from).toList(); }
    @GetMapping("/{id}") @Operation(summary="Consultar pedido")
    public Map<String,Object> find(@PathVariable long id) { return VentaView.from(consultar.obtener(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Crear pedido")
    public Map<String,Object> create(@Valid @RequestBody VentaDto.Pedido input) { return VentaView.from(gestionar.crear(input.toDatos(),uid())); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Actualizar pedido pendiente")
    public Map<String,Object> update(@PathVariable long id, @Valid @RequestBody VentaDto.Pedido input) { return VentaView.from(gestionar.actualizar(id,input.toDatos(),uid())); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Eliminar pedido pendiente sin despacho")
    public void delete(@PathVariable long id) {
        gestionar.eliminar(id,uid());
    }
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Preparar o cancelar pedido")
    public Map<String,Object> estado(@PathVariable long id,
            @Valid @RequestBody VentaDto.EstadoPedido input) {
        return VentaView.from(gestionar.cambiarEstado(id,input.toEstado(),uid()));
    }

    @GetMapping("/{id}/detalles") @Operation(summary="Listar productos del pedido")
    public List<Map<String,Object>> details(@PathVariable long id) { return consultar.listarDetalles(id).stream().map(VentaView::from).toList(); }
    @GetMapping("/{id}/detalles/{productoId}") @Operation(summary="Consultar producto del pedido")
    public Map<String,Object> detail(@PathVariable long id, @PathVariable long productoId) {
        return VentaView.from(consultar.obtenerDetalle(id,productoId));
    }
    @PostMapping("/{id}/detalles") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Agregar producto al pedido pendiente")
    public Map<String,Object> createDetail(@PathVariable long id,
            @Valid @RequestBody VentaDto.PedidoDetalle input) {
        return VentaView.from(detalles.agregar(id,input.toDatos(),uid()));
    }
    @PutMapping("/{id}/detalles/{productoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Actualizar producto del pedido pendiente")
    public Map<String,Object> updateDetail(@PathVariable long id, @PathVariable long productoId,
            @Valid @RequestBody VentaDto.PedidoDetalle input) {
        return VentaView.from(detalles.actualizar(id,productoId,input.toDatos(),uid()));
    }
    @DeleteMapping("/{id}/detalles/{productoId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Eliminar producto del pedido pendiente")
    public void deleteDetail(@PathVariable long id, @PathVariable long productoId) { detalles.eliminar(id,productoId,uid()); }
}
