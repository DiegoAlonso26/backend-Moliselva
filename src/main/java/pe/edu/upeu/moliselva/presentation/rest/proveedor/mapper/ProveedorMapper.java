package pe.edu.upeu.moliselva.presentation.rest.proveedor.mapper;

import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;
import pe.edu.upeu.moliselva.domain.proveedor.ProveedorDatos;
import pe.edu.upeu.moliselva.presentation.rest.proveedor.dto.ProveedorDto;

public final class ProveedorMapper {
    private ProveedorMapper() {}

    public static ProveedorDatos aDatos(ProveedorDto.Datos input) {
        return new ProveedorDatos(input.razonSocial(),input.documento(),input.telefono());
    }

    public static ProveedorDto aRespuesta(Proveedor proveedor) {
        return new ProveedorDto(proveedor.idProveedor(),proveedor.razonSocial(),
            proveedor.documento(),proveedor.telefono());
    }
}
