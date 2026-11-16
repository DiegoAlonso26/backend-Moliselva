package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.application.shared.FallaNegocio;

/** Falla de producción. {@code status} se conserva para las pruebas y equivale al {@link #tipo()}: 400, 404 o 409. */
public class ProduccionFailure extends FallaNegocio {
    private final String codigo;
    private final int status;
    public ProduccionFailure(int status, String codigo, String message) {
        super(message);
        this.codigo = codigo;
        this.status = status;
    }
    @Override
    public String codigo() { return codigo; }
    public int status() { return status; }

    @Override
    public Tipo tipo() {
        return switch (status) {
            case 400 -> Tipo.DATOS_INVALIDOS;
            case 403 -> Tipo.SIN_PERMISO;
            case 404 -> Tipo.NO_ENCONTRADO;
            case 409 -> Tipo.CONFLICTO;
            default -> throw new IllegalStateException("Estado de falla de producción no soportado: " + status);
        };
    }
}
