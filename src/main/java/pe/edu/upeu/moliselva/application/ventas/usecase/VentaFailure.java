package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.application.shared.FallaNegocio;

public class VentaFailure extends FallaNegocio {
    public enum Type { DATOS_INVALIDOS, NO_ENCONTRADO, CONFLICTO }
    private final Type type;
    private final String codigo;
    public VentaFailure(Type type, String codigo, String message) {
        super(message);
        this.type=type;
        this.codigo=codigo;
    }
    public Type type() { return type; }
    @Override
    public String codigo() { return codigo; }

    @Override
    public Tipo tipo() {
        return switch (type) {
            case DATOS_INVALIDOS -> Tipo.DATOS_INVALIDOS;
            case NO_ENCONTRADO -> Tipo.NO_ENCONTRADO;
            case CONFLICTO -> Tipo.CONFLICTO;
        };
    }
}
