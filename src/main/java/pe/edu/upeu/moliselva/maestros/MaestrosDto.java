package pe.edu.upeu.moliselva.maestros;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class MaestrosDto {
    private MaestrosDto() {}

    public record Cliente(long idCliente, String tipoCliente, String nombreRazonSocial,
            String documento, String telefono, String zona) {}
    public record ClienteDatos(
        @Schema(example="REVENDEDOR") @NotBlank @Pattern(regexp="REVENDEDOR|CRIADOR_TECNIFICADO|OTRO") String tipoCliente,
        @Schema(example="Cliente de prueba") @NotBlank @Size(max=160) String nombreRazonSocial,
        @Schema(example="12345678") @Size(max=30) String documento,
        @Schema(example="987654321") @Size(max=30) String telefono,
        @Schema(example="Lima") @Size(max=120) String zona) {}

    public record Insumo(long idInsumo, String nombre, String unidadMedida) {}
    public record InsumoDatos(
        @Schema(example="Sorgo") @NotBlank @Size(max=120) String nombre,
        @Schema(example="kg", description="Opcional; por defecto kg") @Size(max=20) String unidadMedida) {}

    public record Producto(long idProducto, String nombre, String especie, String fase,
            String lineaComercial, String presentacion) {}
    public record ProductoDatos(
        @Schema(example="Balanceado bovino de prueba") @NotBlank @Size(max=160) String nombre,
        @Schema(example="Bovino") @Size(max=60) String especie,
        @Schema(example="Crecimiento") @Size(max=60) String fase,
        @Schema(example="GJA") @Pattern(regexp="GJA|MCDO|OTRA") String lineaComercial,
        @Schema(example="Saco de 40 kg", description="Opcional; por defecto Saco de 40 kg") @Size(max=80) String presentacion) {}
}
