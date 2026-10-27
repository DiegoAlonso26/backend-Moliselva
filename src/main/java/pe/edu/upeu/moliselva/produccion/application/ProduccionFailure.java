package pe.edu.upeu.moliselva.produccion.application;

public class ProduccionFailure extends RuntimeException {
    private final String codigo;
    private final int status;
    public ProduccionFailure(int status, String codigo, String message) {
        super(message);
        this.codigo = codigo;
        this.status = status;
    }
    public String codigo() { return codigo; }
    public int status() { return status; }
}
