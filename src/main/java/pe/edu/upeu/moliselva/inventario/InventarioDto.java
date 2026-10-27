package pe.edu.upeu.moliselva.inventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class InventarioDto {
    private InventarioDto() {}
    public record Kardex(long idMovimiento, LocalDateTime fechaHora, String tipoMovimiento,
        BigDecimal cantidadKg, BigDecimal saldoKg, Long idRecepcion, String usuario) {}
    public record Stock(BigDecimal stockKg) {}
}
