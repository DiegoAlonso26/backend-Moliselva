package pe.edu.upeu.moliselva.application.shared;

/** Falla de negocio con tipo y código explícitos; la usan los módulos que no necesitan una falla propia. */
public class FallaAplicacion extends FallaNegocio {
    private final Tipo tipo;
    private final String codigo;

    public FallaAplicacion(Tipo tipo, String codigo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
        this.codigo = codigo;
    }

    @Override public Tipo tipo() { return tipo; }
    @Override public String codigo() { return codigo; }

    public static FallaAplicacion noEncontrado(String codigo, String mensaje) {
        return new FallaAplicacion(Tipo.NO_ENCONTRADO, codigo, mensaje);
    }

    public static FallaAplicacion conflicto(String codigo, String mensaje) {
        return new FallaAplicacion(Tipo.CONFLICTO, codigo, mensaje);
    }

    public static FallaAplicacion datosInvalidos(String codigo, String mensaje) {
        return new FallaAplicacion(Tipo.DATOS_INVALIDOS, codigo, mensaje);
    }
}
