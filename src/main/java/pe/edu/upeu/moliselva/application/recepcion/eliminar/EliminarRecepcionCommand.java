package pe.edu.upeu.moliselva.application.recepcion.eliminar;

import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.cqrs.Command;

/** Eliminar una recepción no decidida y sin entrada al kardex. No devuelve respuesta. */
public record EliminarRecepcionCommand(long idRecepcion, Actor actor) implements Command<Void> {}
