package pe.edu.upeu.moliselva.domain.formulas;

/** Datos editables de una fórmula: versión sin espacios sobrantes y vigente por defecto. */
public record FormulaDatos(long idProducto, String version, boolean vigente) {
    public static FormulaDatos de(long idProducto, String version, Boolean vigente) {
        return new FormulaDatos(idProducto, version.trim(), vigente == null || vigente);
    }
}
