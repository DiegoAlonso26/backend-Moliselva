package pe.edu.upeu.moliselva.recepcion.application;

public class RecepcionFailure extends RuntimeException {
    public enum Reason {
        SIN_PERMISO, PROVEEDOR_INVALIDO, MAIZ_NO_CONFIGURADO,
        PESOS_INVALIDOS, HUMEDAD_FUERA_DE_RANGO,
        RECEPCION_NO_ENCONTRADA, RECEPCION_NO_PENDIENTE, RECEPCION_NO_EDITABLE
    }
    private final Reason reason;
    public RecepcionFailure(Reason reason, String message) {
        super(message);
        this.reason=reason;
    }
    public Reason reason() { return reason; }
}
