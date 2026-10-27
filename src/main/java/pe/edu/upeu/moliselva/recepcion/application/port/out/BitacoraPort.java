package pe.edu.upeu.moliselva.recepcion.application.port.out;

public interface BitacoraPort {
    void registrar(long usuarioId, String accion, String motivo);
}
