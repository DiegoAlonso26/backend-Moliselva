package pe.edu.upeu.moliselva.recepcion.application.port.in;

import pe.edu.upeu.moliselva.recepcion.application.ActorRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.DecisionRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;

public interface DecidirRecepcionUseCase {
    Recepcion decidir(long id, DecisionRecepcion decision, String motivo, ActorRecepcion actor);
}
