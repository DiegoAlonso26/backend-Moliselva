package pe.edu.upeu.moliselva.application.recepcion.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.domain.recepcion.DecisionRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;

public interface DecidirRecepcionUseCase {
    Recepcion decidir(long id, DecisionRecepcion decision, String motivo, Actor actor);
}
