package pe.edu.upeu.moliselva.domain.maestros;

/** Datos editables de un cliente, normalizados: nombre sin espacios sobrantes y campos vacíos como null. */
public record ClienteDatos(String tipoCliente, String nombreRazonSocial, String documento, String telefono, String zona) {
    public ClienteDatos {
        nombreRazonSocial = nombreRazonSocial.trim();
        documento = Maestros.opcional(documento);
        telefono = Maestros.opcional(telefono);
        zona = Maestros.opcional(zona);
    }
}
