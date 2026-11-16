package pe.edu.upeu.moliselva.application.recepcion.listar;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.shared.cqrs.Query;

/** Recepciones de un día, en orden de llegada. */
public record ListarRecepcionesQuery(LocalDate fecha) implements Query<List<RecepcionResponse>> {}
