package pe.edu.upeu.moliselva.inventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public final class InventarioDto {
    private InventarioDto() {}
    public record Kardex(long idMovimiento, LocalDateTime fechaHora, String tipoMovimiento,
        BigDecimal cantidadKg, BigDecimal saldoKg, Long idRecepcion, String usuario) {}
    public record Stock(BigDecimal stockKg) {}
    public record ItemStock(String tipoItem, long idItem, String nombre,
        String unidadMedida, BigDecimal stockActual) {}
    public record Ajuste(
        @Schema(example="AJUSTE_ENTRADA") @NotBlank @Pattern(regexp="AJUSTE_ENTRADA|AJUSTE_SALIDA") String tipoMovimiento,
        @Schema(description="Envíe este ID o idProducto, nunca ambos") Long idInsumo,
        @Schema(description="Envíe este ID o idInsumo, nunca ambos") Long idProducto,
        @Schema(example="25.5") @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=11,fraction=3) BigDecimal cantidad,
        @Schema(example="Conteo físico de inventario") @NotBlank @Size(max=300) String motivo) {}
    public record Movimiento(long idMovimiento, LocalDateTime fechaHora, String tipoMovimiento,
        Long idInsumo, Long idProducto, BigDecimal cantidad, String unidadMedida,
        Long idRecepcion, Long idLote, Long idDespacho, String usuario) {}
}
