package pe.edu.upeu.moliselva.domain.maestros;

/** Reglas comunes de los datos maestros. */
public final class Maestros {
    /** Insumo que sostiene el kardex de la demo; no se modifica ni se desactiva. */
    public static final String INSUMO_SISTEMA = "Maíz";
    public static final String UNIDAD_POR_DEFECTO = "kg";
    public static final String PRESENTACION_POR_DEFECTO = "Saco de 40 kg";

    private Maestros() {}

    public static boolean esInsumoSistema(Insumo insumo) {
        return INSUMO_SISTEMA.equals(insumo.nombre());
    }

    static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    static String conDefecto(String valor, String defecto) {
        return valor == null || valor.isBlank() ? defecto : valor.trim();
    }
}
