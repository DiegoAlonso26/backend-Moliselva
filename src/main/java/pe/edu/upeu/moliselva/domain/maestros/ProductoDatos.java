package pe.edu.upeu.moliselva.domain.maestros;

/** Datos editables de un producto; sin presentación se registra como «Saco de 40 kg». */
public record ProductoDatos(String nombre, String especie, String fase, String lineaComercial, String presentacion) {
    public ProductoDatos {
        nombre = nombre.trim();
        especie = Maestros.opcional(especie);
        fase = Maestros.opcional(fase);
        presentacion = Maestros.conDefecto(presentacion, Maestros.PRESENTACION_POR_DEFECTO);
    }
}
