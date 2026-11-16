package pe.edu.upeu.moliselva.application.recepcion.decidir;

import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.cqrs.Command;
import pe.edu.upeu.moliselva.domain.recepcion.DecisionRecepcion;

/** El JEFE_PLANTA acepta o rechaza una recepción PENDIENTE, con un motivo. */
public record DecidirRecepcionCommand(long idRecepcion, DecisionRecepcion decision, String motivo, Actor actor)
    implements Command<RecepcionResponse> {}
