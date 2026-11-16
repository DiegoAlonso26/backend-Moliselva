package pe.edu.upeu.moliselva.domain.maestros;

/** Datos editables de un insumo; sin unidad se registra en kg. */
public record InsumoDatos(String nombre, String unidadMedida) {
    public InsumoDatos {
        nombre = nombre.trim();
        unidadMedida = Maestros.conDefecto(unidadMedida, Maestros.UNIDAD_POR_DEFECTO);
    }
}
