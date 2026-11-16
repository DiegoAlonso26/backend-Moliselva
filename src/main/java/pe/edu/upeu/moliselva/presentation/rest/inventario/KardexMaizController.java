package pe.edu.upeu.moliselva.presentation.rest.inventario;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.moliselva.application.inventario.kardex.exportar.ExportarKardexMaizCommand;
import pe.edu.upeu.moliselva.application.inventario.kardex.listar.ListarKardexMaizQuery;
import pe.edu.upeu.moliselva.application.inventario.kardex.stock.ConsultarStockMaizQuery;
import pe.edu.upeu.moliselva.application.shared.ActorActual;
import pe.edu.upeu.moliselva.application.shared.cqrs.Mediador;
import pe.edu.upeu.moliselva.presentation.rest.inventario.dto.KardexMaizDto;
import pe.edu.upeu.moliselva.presentation.rest.inventario.mapper.KardexMaizMapper;

@RestController
@RequestMapping("/api/inventario/maiz")
@Tag(name = "Inventario de maiz")
@SecurityRequirement(name = "bearerAuth")
public class KardexMaizController {
    private final Mediador mediador;
    private final ActorActual actor;

    public KardexMaizController(Mediador mediador, ActorActual actor) {
        this.mediador=mediador; this.actor=actor;
    }

    @GetMapping("/kardex")
    @Operation(summary = "Consultar kardex")
    public List<KardexMaizDto.Kardex> kardex() {
        return mediador.enviar(new ListarKardexMaizQuery()).stream().map(KardexMaizMapper::aRespuesta).toList();
    }

    @GetMapping("/stock")
    @Operation(summary = "Consultar stock actual")
    public KardexMaizDto.Stock stock() { return KardexMaizMapper.aStock(mediador.enviar(new ConsultarStockMaizQuery())); }

    @GetMapping("/kardex/exportar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','JEFE_PLANTA','CONTADORA')")
    @Operation(summary = "Descargar kardex CSV", description = "ADMINISTRADOR, JEFE_PLANTA o CONTADORA.")
    public ResponseEntity<byte[]> exportar() {
        String filename = "kardex-maiz-"+LocalDate.now(ZoneId.of("America/Lima"))+".csv";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+filename+"\"")
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .body(mediador.enviar(new ExportarKardexMaizCommand(actor.obtener())).csv());
    }
}
