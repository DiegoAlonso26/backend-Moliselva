package pe.edu.upeu.moliselva.application.recepcion;

import pe.edu.upeu.moliselva.application.shared.FallaNegocio;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionInvalida;

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

    /** Traduce una regla del dominio a la falla de aplicación con el mismo código y mensaje. */
    public static RecepcionFailure de(RecepcionInvalida regla) {
        return new RecepcionFailure(Reason.valueOf(regla.regla().name()),regla.getMessage());
    }

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
