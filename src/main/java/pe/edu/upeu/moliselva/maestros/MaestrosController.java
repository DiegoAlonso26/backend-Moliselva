package pe.edu.upeu.moliselva.maestros;

import java.util.List;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name="Clientes, insumos y productos", description="Clientes: lectura ADMINISTRADOR, JEFE_PLANTA y CONTADORA. Insumos y productos: lectura de los cuatro roles. Escritura: ADMINISTRADOR o JEFE_PLANTA.")
@SecurityRequirement(name="bearerAuth")
public class MaestrosController {
    private final MaestrosService service;
    public MaestrosController(MaestrosService service) { this.service=service; }
    private long uid(Jwt jwt) { return ((Number)jwt.getClaim("uid")).longValue(); }

    @GetMapping("/clientes") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Listar clientes activos")
    public List<MaestrosDto.Cliente> clientes() { return service.clientes(); }
    @GetMapping("/clientes/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Consultar cliente")
    public MaestrosDto.Cliente cliente(@PathVariable long id) { return service.cliente(id); }
    @PostMapping("/clientes") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Crear cliente")
    public MaestrosDto.Cliente crearCliente(@Valid @RequestBody MaestrosDto.ClienteDatos input,
            @AuthenticationPrincipal Jwt jwt) { return service.crearCliente(input,uid(jwt)); }
    @PutMapping("/clientes/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary="Actualizar cliente")
    public MaestrosDto.Cliente actualizarCliente(@PathVariable long id,
            @Valid @RequestBody MaestrosDto.ClienteDatos input, @AuthenticationPrincipal Jwt jwt) {
        return service.actualizarCliente(id,input,uid(jwt));
    }
    @DeleteMapping("/clientes/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Desactivar cliente")
    public void eliminarCliente(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminarCliente(id,uid(jwt));
    }

    @GetMapping("/insumos") @Operation(summary="Listar insumos activos")
    public List<MaestrosDto.Insumo> insumos() { return service.insumos(); }
    @GetMapping("/insumos/{id}") @Operation(summary="Consultar insumo")
    public MaestrosDto.Insumo insumo(@PathVariable long id) { return service.insumo(id); }
    @PostMapping("/insumos") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Crear insumo")
    public MaestrosDto.Insumo crearInsumo(@Valid @RequestBody MaestrosDto.InsumoDatos input,
            @AuthenticationPrincipal Jwt jwt) { return service.crearInsumo(input,uid(jwt)); }
    @PutMapping("/insumos/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary="Actualizar insumo")
    public MaestrosDto.Insumo actualizarInsumo(@PathVariable long id,
            @Valid @RequestBody MaestrosDto.InsumoDatos input, @AuthenticationPrincipal Jwt jwt) {
        return service.actualizarInsumo(id,input,uid(jwt));
    }
    @DeleteMapping("/insumos/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Desactivar insumo")
    public void eliminarInsumo(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminarInsumo(id,uid(jwt));
    }

    @GetMapping("/productos") @Operation(summary="Listar productos activos")
    public List<MaestrosDto.Producto> productos() { return service.productos(); }
    @GetMapping("/productos/{id}") @Operation(summary="Consultar producto")
    public MaestrosDto.Producto producto(@PathVariable long id) { return service.producto(id); }
    @PostMapping("/productos") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Crear producto")
    public MaestrosDto.Producto crearProducto(@Valid @RequestBody MaestrosDto.ProductoDatos input,
            @AuthenticationPrincipal Jwt jwt) { return service.crearProducto(input,uid(jwt)); }
    @PutMapping("/productos/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary="Actualizar producto")
    public MaestrosDto.Producto actualizarProducto(@PathVariable long id,
            @Valid @RequestBody MaestrosDto.ProductoDatos input, @AuthenticationPrincipal Jwt jwt) {
        return service.actualizarProducto(id,input,uid(jwt));
    }
    @DeleteMapping("/productos/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Desactivar producto")
    public void eliminarProducto(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminarProducto(id,uid(jwt));
    }
}
