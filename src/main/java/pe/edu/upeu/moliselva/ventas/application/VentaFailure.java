package pe.edu.upeu.moliselva.ventas.application;

public class VentaFailure extends RuntimeException {
    public enum Type { DATOS_INVALIDOS, NO_ENCONTRADO, CONFLICTO }
    private final Type type;
    private final String codigo;
    public VentaFailure(Type type, String codigo, String message) {
        super(message);
        this.type=type;
        this.codigo=codigo;
    }
    public Type type() { return type; }
    public String codigo() { return codigo; }
}
