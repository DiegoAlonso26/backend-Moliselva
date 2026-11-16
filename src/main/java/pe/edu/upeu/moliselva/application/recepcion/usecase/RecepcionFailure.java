package pe.edu.upeu.moliselva.application.recepcion.usecase;

import pe.edu.upeu.moliselva.application.shared.FallaNegocio;

public class RecepcionFailure extends FallaNegocio {
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

    @Override
    public Tipo tipo() {
        return switch (reason) {
            case SIN_PERMISO -> Tipo.SIN_PERMISO;
            case PROVEEDOR_INVALIDO, PESOS_INVALIDOS, HUMEDAD_FUERA_DE_RANGO -> Tipo.DATOS_INVALIDOS;
            case RECEPCION_NO_ENCONTRADA -> Tipo.NO_ENCONTRADO;
            case MAIZ_NO_CONFIGURADO, RECEPCION_NO_PENDIENTE, RECEPCION_NO_EDITABLE -> Tipo.CONFLICTO;
        };
    }

    @Override
    public String codigo() { return reason.name(); }
}
