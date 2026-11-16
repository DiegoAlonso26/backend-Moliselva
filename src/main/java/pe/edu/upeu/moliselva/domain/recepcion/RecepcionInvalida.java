package pe.edu.upeu.moliselva.domain.recepcion;

/**
 * Una regla de la recepción de maíz no se cumple. Lo lanzan la entidad {@link Recepcion}
 * y sus value objects; la capa de aplicación lo traduce a su falla con el mismo código.
 */
public class RecepcionInvalida extends IllegalArgumentException {
    public enum Regla { PESOS_INVALIDOS, HUMEDAD_FUERA_DE_RANGO, RECEPCION_NO_PENDIENTE, RECEPCION_NO_EDITABLE }

    static final String NO_EDITABLE = "La recepción aceptada o decidida no se puede modificar ni eliminar";

    private final Regla regla;

    public RecepcionInvalida(Regla regla, String mensaje) {
        super(mensaje);
        this.regla = regla;
    }

    public Regla regla() { return regla; }
}
