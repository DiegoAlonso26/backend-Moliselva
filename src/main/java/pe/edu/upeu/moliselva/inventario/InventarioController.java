package pe.edu.upeu.moliselva.inventario;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventario/maiz")
@Tag(name = "Inventario de maiz")
@SecurityRequirement(name = "bearerAuth")
public class InventarioController {
    private final InventarioService service;
    public InventarioController(InventarioService service) { this.service=service; }

    @GetMapping("/kardex")
    @Operation(summary = "Consultar kardex")
    public List<InventarioDto.Kardex> kardex() { return service.kardex(); }

    @GetMapping("/stock")
    @Operation(summary = "Consultar stock actual")
    public InventarioDto.Stock stock() { return service.stock(); }

    @GetMapping("/kardex/exportar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary = "Descargar kardex CSV", description = "ADMINISTRADOR, JEFE_PLANTA o CONTADORA.")
    public ResponseEntity<byte[]> exportar(@AuthenticationPrincipal Jwt jwt) {
        long userId = ((Number)jwt.getClaim("uid")).longValue();
        String filename = "kardex-maiz-"+LocalDate.now(ZoneId.of("America/Lima"))+".csv";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+filename+"\"")
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .body(service.exportarCsv(userId));
    }
}
