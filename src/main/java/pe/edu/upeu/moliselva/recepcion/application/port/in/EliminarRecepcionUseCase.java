package pe.edu.upeu.moliselva.recepcion.application.port.in;

import pe.edu.upeu.moliselva.recepcion.application.ActorRecepcion;

public interface EliminarRecepcionUseCase {
    void eliminar(long id, ActorRecepcion actor);
}
