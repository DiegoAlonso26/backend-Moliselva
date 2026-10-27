package pe.edu.upeu.moliselva.recepcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class RecepcionDto {
    private RecepcionDto() {}
    public record Crear(
        @Schema(example="1") @NotNull @Positive Long idProveedor,
        @Schema(example="32500") @NotNull @Positive @Digits(integer=10,fraction=2) BigDecimal pesoBrutoKg,
        @Schema(example="12500") @NotNull @PositiveOrZero @Digits(integer=10,fraction=2) BigDecimal taraKg,
        @Schema(example="0") @NotNull @PositiveOrZero @Digits(integer=10,fraction=2) BigDecimal pesoSacosKg,
        @Schema(example="14.2") @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer=3,fraction=2) BigDecimal humedadPct,
        @Schema(example="150") @NotNull @PositiveOrZero @Digits(integer=10,fraction=2) BigDecimal mermaLimpiezaKg) {}

    public record Decision(
        @Schema(allowableValues={"ACEPTAR","RECHAZAR"}, example="ACEPTAR")
        @NotBlank @Pattern(regexp="ACEPTAR|RECHAZAR") String decision,
        @Schema(example="Calidad aprobada tras revision") @NotBlank @Size(max=300) String motivo) {}

    public record Respuesta(
        long idRecepcion, LocalDateTime fechaHora, String proveedor,
        BigDecimal pesoBrutoKg, BigDecimal taraKg, BigDecimal pesoSacosKg,
        BigDecimal pesoNetoKg, BigDecimal humedadPct, BigDecimal mermaLimpiezaKg,
        String estado, BigDecimal entradaKardexKg, String registradoPor, String autorizadoPor) {
        public Respuesta conRegistradoPor(String usuario) {
            return new Respuesta(idRecepcion,fechaHora,proveedor,pesoBrutoKg,taraKg,pesoSacosKg,
                pesoNetoKg,humedadPct,mermaLimpiezaKg,estado,entradaKardexKg,usuario,autorizadoPor);
        }
    }
}
