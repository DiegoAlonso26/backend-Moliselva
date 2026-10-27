package pe.edu.upeu.moliselva.operaciones;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    public record Lote(@Schema(example="1") @NotNull @Positive Long idProducto,
        @Schema(example="1") @NotNull @Positive Long idFormula,
        @Schema(example="500",allowableValues={"500","1000"}) @NotNull @Min(500) Integer tamanoKg) {}
    public record LoteInsumo(@Schema(example="1") @NotNull @Positive Long idInsumo,
        @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=9,fraction=3)
        @Schema(example="250") BigDecimal cantidadCalculadaKg,
        @DecimalMin(value="0",inclusive=false) @Digits(integer=9,fraction=3)
        @Schema(example="249") BigDecimal cantidadRealKg) {}
    public record EstadoLote(@Schema(example="EN_PROCESO") @NotBlank @Pattern(regexp="EN_PROCESO|ANULADO") String estado) {}
    public record Pedido(@Schema(example="1") @NotNull @Positive Long idCliente,
        @Schema(example="Pedido de prueba") @Size(max=300) String observacion) {}
    public record EstadoPedido(@Schema(example="EN_PREPARACION") @NotBlank @Pattern(regexp="EN_PREPARACION|CANCELADO") String estado) {}
    public record PedidoDetalle(@Schema(example="1") @NotNull @Positive Long idProducto,
        @Schema(example="2") @NotNull @Positive Integer cantidadSacos,
        @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=10,fraction=2)
        @Schema(example="80") BigDecimal cantidadKg) {}
    public record Despacho(@Schema(example="1") @NotNull @Positive Long idPedido,
        @Schema(example="REPARTO") @NotBlank @Pattern(regexp="RECOJO_PLANTA|REPARTO") String tipoEntrega,
        @Schema(example="Lima") @Size(max=160) String destino) {}
    public record DespachoDetalle(@Schema(example="1") @NotNull @Positive Long idProducto,
        @Schema(example="2") @NotNull @Positive Integer cantidadSacos,
        @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=10,fraction=2)
        @Schema(example="80") BigDecimal cantidadKg) {}
}
