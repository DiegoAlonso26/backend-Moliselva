package pe.edu.upeu.moliselva.proveedor;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores")
@SecurityRequirement(name = "bearerAuth")
public class ProveedorController {
    private final ProveedorService service;
    public ProveedorController(ProveedorService service) { this.service=service; }
    @GetMapping
    @Operation(summary = "Listar proveedores activos")
    public List<ProveedorDto> list() { return service.list(); }
}
