package pe.edu.upeu.moliselva.application.recepcion.obtener;

import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.shared.cqrs.Query;

public record ObtenerRecepcionQuery(long idRecepcion) implements Query<RecepcionResponse> {}
