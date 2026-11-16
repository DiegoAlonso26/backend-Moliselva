package pe.edu.upeu.moliselva.application.shared;

/**
 * Falla de una regla de negocio. La capa de presentación la traduce a HTTP
 * según su {@link Tipo}; el {@link #codigo()} viaja tal cual en el JSON.
 */
public abstract class FallaNegocio extends RuntimeException {
    public enum Tipo { SIN_PERMISO, DATOS_INVALIDOS, NO_ENCONTRADO, CONFLICTO }

    protected FallaNegocio(String mensaje) {
        super(mensaje);
    }

    public abstract Tipo tipo();

    public abstract String codigo();
}
