package pe.edu.upeu.moliselva.recepcion.application.port.in;

import pe.edu.upeu.moliselva.recepcion.application.ActorRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionDatos;

public interface CorregirRecepcionUseCase {
    Recepcion corregir(long id, RecepcionDatos datos, ActorRecepcion actor);
}
