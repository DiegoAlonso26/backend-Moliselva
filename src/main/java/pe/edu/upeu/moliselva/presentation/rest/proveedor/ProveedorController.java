package pe.edu.upeu.moliselva.presentation.rest.proveedor;

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
import pe.edu.upeu.moliselva.application.proveedor.usecase.ConsultarProveedoresUseCase;
import pe.edu.upeu.moliselva.application.proveedor.usecase.GestionarProveedorUseCase;
import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.presentation.rest.proveedor.dto.ProveedorDto;
import pe.edu.upeu.moliselva.presentation.rest.proveedor.mapper.ProveedorMapper;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores")
@SecurityRequirement(name = "bearerAuth")
public class ProveedorController {
    private final ConsultarProveedoresUseCase consultar;
    private final GestionarProveedorUseCase gestionar;
    private final ActorActual actor;

    public ProveedorController(ConsultarProveedoresUseCase consultar,
            GestionarProveedorUseCase gestionar, ActorActual actor) {
        this.consultar=consultar; this.gestionar=gestionar; this.actor=actor;
    }

    @GetMapping
    @Operation(summary = "Listar proveedores activos")
    public List<ProveedorDto> list() {
        return consultar.listar().stream().map(ProveedorMapper::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar proveedor activo")
    public ProveedorDto find(@PathVariable long id) {
        return ProveedorMapper.aRespuesta(consultar.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary = "Crear proveedor", description = "ADMINISTRADOR o JEFE_PLANTA.")
    public ProveedorDto crear(@Valid @RequestBody ProveedorDto.Datos input) {
        return ProveedorMapper.aRespuesta(gestionar.crear(ProveedorMapper.aDatos(input),actor.obtener()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary = "Actualizar proveedor", description = "ADMINISTRADOR o JEFE_PLANTA.")
    public ProveedorDto actualizar(@PathVariable long id, @Valid @RequestBody ProveedorDto.Datos input) {
        return ProveedorMapper.aRespuesta(gestionar.actualizar(id,ProveedorMapper.aDatos(input),actor.obtener()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary = "Desactivar proveedor", description = "Borrado lógico para conservar las recepciones históricas. ADMINISTRADOR o JEFE_PLANTA.")
    public void eliminar(@PathVariable long id) {
        gestionar.desactivar(id,actor.obtener());
    }
}
