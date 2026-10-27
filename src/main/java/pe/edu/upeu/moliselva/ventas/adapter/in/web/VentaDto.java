package pe.edu.upeu.moliselva.ventas.adapter.in.web;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pe.edu.upeu.moliselva.ventas.domain.DatosPedido;
import pe.edu.upeu.moliselva.ventas.domain.DatosDespacho;
import pe.edu.upeu.moliselva.ventas.domain.DatosDetalle;
import pe.edu.upeu.moliselva.ventas.domain.TipoEntrega;

public final class VentaDto {
    private VentaDto() {}
    public record Pedido(@Schema(example="1") @NotNull @Positive Long idCliente,
        @Schema(example="Pedido de prueba") @Size(max=300) String observacion) {
        public DatosPedido toDatos() { return new DatosPedido(idCliente,observacion); }
    }
    public record EstadoPedido(@Schema(example="EN_PREPARACION") @NotBlank
        @Pattern(regexp="EN_PREPARACION|CANCELADO") String estado) {
        public pe.edu.upeu.moliselva.ventas.domain.EstadoPedido toEstado() {
            return pe.edu.upeu.moliselva.ventas.domain.EstadoPedido.valueOf(estado);
        }
    }
    public record PedidoDetalle(@Schema(example="1") @NotNull @Positive Long idProducto,
        @Schema(example="2") @NotNull @Positive Integer cantidadSacos,
        @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=10,fraction=2)
        @Schema(example="80") BigDecimal cantidadKg) {
        public DatosDetalle toDatos() { return new DatosDetalle(idProducto,cantidadSacos,cantidadKg); }
    }
    public record Despacho(@Schema(example="1") @NotNull @Positive Long idPedido,
        @Schema(example="REPARTO") @NotBlank @Pattern(regexp="RECOJO_PLANTA|REPARTO") String tipoEntrega,
        @Schema(example="Lima") @Size(max=160) String destino) {
        public DatosDespacho toDatos() {
            return new DatosDespacho(idPedido,TipoEntrega.valueOf(tipoEntrega),destino);
        }
    }
    public record DespachoDetalle(@Schema(example="1") @NotNull @Positive Long idProducto,
        @Schema(example="2") @NotNull @Positive Integer cantidadSacos,
        @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=10,fraction=2)
        @Schema(example="80") BigDecimal cantidadKg) {
        public DatosDetalle toDatos() { return new DatosDetalle(idProducto,cantidadSacos,cantidadKg); }
    }
}
