package pe.edu.upeu.moliselva.produccion.application.port.out;

public interface BitacoraProduccionPort {
    void registrar(long usuarioId, String accion);
}
