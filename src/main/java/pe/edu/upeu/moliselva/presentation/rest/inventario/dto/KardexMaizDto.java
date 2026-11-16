package pe.edu.upeu.moliselva.presentation.rest.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Contrato JSON de /api/inventario/maiz. */
public final class KardexMaizDto {
    private KardexMaizDto() {}

    public record Kardex(long idMovimiento, LocalDateTime fechaHora, String tipoMovimiento,
        BigDecimal cantidadKg, BigDecimal saldoKg, Long idRecepcion, String usuario) {}

    public record Stock(BigDecimal stockKg) {}
}
