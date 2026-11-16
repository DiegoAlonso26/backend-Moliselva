package pe.edu.upeu.moliselva.presentation.rest.bitacora;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.bitacora.usecase.ConsultarBitacoraUseCase;

@RestController
@RequestMapping("/api/bitacora")
@Tag(name = "Bitacora")
@SecurityRequirement(name = "bearerAuth")
public class BitacoraController {
    private final ConsultarBitacoraUseCase bitacora;

    public BitacoraController(ConsultarBitacoraUseCase bitacora) { this.bitacora = bitacora; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary = "Consultar eventos recientes", description = "ADMINISTRADOR o JEFE_PLANTA.")
    public List<Map<String,Object>> recientes() { return bitacora.recientes(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA')")
    @Operation(summary="Consultar evento", description="La bitácora es inmutable.")
    public Map<String,Object> find(@PathVariable long id) { return bitacora.obtener(id); }
}
