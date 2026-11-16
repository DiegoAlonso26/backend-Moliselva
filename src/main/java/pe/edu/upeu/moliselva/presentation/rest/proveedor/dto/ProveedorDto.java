package pe.edu.upeu.moliselva.presentation.rest.proveedor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contrato JSON de /api/proveedores. La conversión está en ProveedorMapper. */
public record ProveedorDto(long idProveedor, String razonSocial, String documento, String telefono) {
    public record Datos(
        @Schema(example="Proveedor de maíz SAC") @NotBlank @Size(max=160) String razonSocial,
        @Schema(example="20123456789") @Size(max=30) String documento,
        @Schema(example="987654321") @Size(max=30) String telefono
    ) {}
}
