package pe.edu.upeu.moliselva.produccion.adapter.in.web;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import pe.edu.upeu.moliselva.produccion.domain.DatosInsumo;
import pe.edu.upeu.moliselva.produccion.domain.DatosLote;

public final class LoteDto {
    private LoteDto() {}
    public record Lote(@Schema(example="1") @NotNull @Positive Long idProducto,
        @Schema(example="1") @NotNull @Positive Long idFormula,
        @Schema(example="500",allowableValues={"500","1000"}) @NotNull @Min(500) Integer tamanoKg) {
        public DatosLote toDatos() { return new DatosLote(idProducto,idFormula,tamanoKg); }
    }
    public record LoteInsumo(@Schema(example="1") @NotNull @Positive Long idInsumo,
        @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=9,fraction=3)
        @Schema(example="250") BigDecimal cantidadCalculadaKg,
        @DecimalMin(value="0",inclusive=false) @Digits(integer=9,fraction=3)
        @Schema(example="249") BigDecimal cantidadRealKg) {
        public DatosInsumo toDatos() {
            return new DatosInsumo(idInsumo,cantidadCalculadaKg,cantidadRealKg);
        }
    }
    public record EstadoLote(@Schema(example="EN_PROCESO") @NotBlank
        @Pattern(regexp="EN_PROCESO|ANULADO") String estado) {
        public pe.edu.upeu.moliselva.produccion.domain.EstadoLote toEstado() {
            return pe.edu.upeu.moliselva.produccion.domain.EstadoLote.valueOf(estado);
        }
    }
}
