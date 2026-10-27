package pe.edu.upeu.moliselva.operaciones;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class OperacionDto {
    private OperacionDto() {}

    public record Formula(@Schema(example="1") @NotNull @Positive Long idProducto,
        @Schema(example="v1-prueba") @NotBlank @Size(max=40) String version,
        @Schema(example="true",description="Opcional; por defecto true") Boolean vigente) {}
    public record VigenciaFormula(@NotNull Boolean vigente) {}
    public record FormulaDetalle(@Schema(example="1") @NotNull @Positive Long idInsumo,
        @NotNull @DecimalMin(value="0",inclusive=false) @DecimalMax("100")
        @Schema(example="25.0000") @Digits(integer=3,fraction=4) BigDecimal proporcionConfidencial) {}
}
