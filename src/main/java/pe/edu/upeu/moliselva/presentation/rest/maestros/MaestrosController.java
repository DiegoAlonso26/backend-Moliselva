package pe.edu.upeu.moliselva.presentation.rest.maestros;

import java.util.List;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.maestros.usecase.ConsultarMaestrosUseCase;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarClientesUseCase;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarInsumosUseCase;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarProductosUseCase;
import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.presentation.rest.maestros.dto.MaestrosDto;
import pe.edu.upeu.moliselva.presentation.rest.maestros.mapper.MaestrosMapper;

@RestController
@RequestMapping("/api")
@Tag(name="Clientes, insumos y productos", description="Clientes: lectura ADMINISTRADOR, JEFE_PLANTA y CONTADORA. Insumos y productos: lectura de los cuatro roles. Escritura: ADMINISTRADOR o JEFE_PLANTA.")
@SecurityRequirement(name="bearerAuth")
public class MaestrosController {
    private final ConsultarMaestrosUseCase consultar;
    private final GestionarClientesUseCase clientes;
    private final GestionarInsumosUseCase insumos;
    private final GestionarProductosUseCase productos;
    private final ActorActual actor;

    public MaestrosController(ConsultarMaestrosUseCase consultar, GestionarClientesUseCase clientes,
            GestionarInsumosUseCase insumos, GestionarProductosUseCase productos, ActorActual actor) {
        this.consultar=consultar; this.clientes=clientes; this.insumos=insumos;
        this.productos=productos; this.actor=actor;
    }

    private long uid() { return actor.obtener().id(); }

    @GetMapping("/clientes") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Listar clientes activos")
    public List<MaestrosDto.Cliente> clientes() {
        return consultar.clientes().stream().map(MaestrosMapper::aRespuesta).toList();
    }
    @GetMapping("/clientes/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary="Consultar cliente")
    public MaestrosDto.Cliente cliente(@PathVariable long id) { return MaestrosMapper.aRespuesta(consultar.cliente(id)); }
    @PostMapping("/clientes") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Crear cliente")
    public MaestrosDto.Cliente crearCliente(@Valid @RequestBody MaestrosDto.ClienteDatos input) {
        return MaestrosMapper.aRespuesta(clientes.crear(MaestrosMapper.aDatos(input),uid()));
    }
    @PutMapping("/clientes/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary="Actualizar cliente")
    public MaestrosDto.Cliente actualizarCliente(@PathVariable long id,
            @Valid @RequestBody MaestrosDto.ClienteDatos input) {
        return MaestrosMapper.aRespuesta(clientes.actualizar(id,MaestrosMapper.aDatos(input),uid()));
    }
    @DeleteMapping("/clientes/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Desactivar cliente")
    public void eliminarCliente(@PathVariable long id) { clientes.desactivar(id,uid()); }

    @GetMapping("/insumos") @Operation(summary="Listar insumos activos")
    public List<MaestrosDto.Insumo> insumos() {
        return consultar.insumos().stream().map(MaestrosMapper::aRespuesta).toList();
    }
    @GetMapping("/insumos/{id}") @Operation(summary="Consultar insumo")
    public MaestrosDto.Insumo insumo(@PathVariable long id) { return MaestrosMapper.aRespuesta(consultar.insumo(id)); }
    @PostMapping("/insumos") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Crear insumo")
    public MaestrosDto.Insumo crearInsumo(@Valid @RequestBody MaestrosDto.InsumoDatos input) {
        return MaestrosMapper.aRespuesta(insumos.crear(MaestrosMapper.aDatos(input),uid()));
    }
    @PutMapping("/insumos/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary="Actualizar insumo")
    public MaestrosDto.Insumo actualizarInsumo(@PathVariable long id,
            @Valid @RequestBody MaestrosDto.InsumoDatos input) {
        return MaestrosMapper.aRespuesta(insumos.actualizar(id,MaestrosMapper.aDatos(input),uid()));
    }
    @DeleteMapping("/insumos/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Desactivar insumo")
    public void eliminarInsumo(@PathVariable long id) { insumos.desactivar(id,uid()); }

    @GetMapping("/productos") @Operation(summary="Listar productos activos")
    public List<MaestrosDto.Producto> productos() {
        return consultar.productos().stream().map(MaestrosMapper::aRespuesta).toList();
    }
    @GetMapping("/productos/{id}") @Operation(summary="Consultar producto")
    public MaestrosDto.Producto producto(@PathVariable long id) { return MaestrosMapper.aRespuesta(consultar.producto(id)); }
    @PostMapping("/productos") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Crear producto")
    public MaestrosDto.Producto crearProducto(@Valid @RequestBody MaestrosDto.ProductoDatos input) {
        return MaestrosMapper.aRespuesta(productos.crear(MaestrosMapper.aDatos(input),uid()));
    }
    @PutMapping("/productos/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary="Actualizar producto")
    public MaestrosDto.Producto actualizarProducto(@PathVariable long id,
            @Valid @RequestBody MaestrosDto.ProductoDatos input) {
        return MaestrosMapper.aRespuesta(productos.actualizar(id,MaestrosMapper.aDatos(input),uid()));
    }
    @DeleteMapping("/productos/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')") @Operation(summary="Desactivar producto")
    public void eliminarProducto(@PathVariable long id) { productos.desactivar(id,uid()); }
}
