package pe.edu.upeu.moliselva.domain.proveedor;

/**
 * Datos editables de un proveedor, ya normalizados: la razón social sin
 * espacios sobrantes y el documento y el teléfono vacíos convertidos en null.
 */
public record ProveedorDatos(String razonSocial, String documento, String telefono) {

    public ProveedorDatos {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        razonSocial = razonSocial.trim();
        documento = opcional(documento);
        telefono = opcional(telefono);
    }

    private static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
