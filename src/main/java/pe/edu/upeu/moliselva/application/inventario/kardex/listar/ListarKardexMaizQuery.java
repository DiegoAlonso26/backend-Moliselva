package pe.edu.upeu.moliselva.application.inventario.kardex.listar;

import java.util.List;
import pe.edu.upeu.moliselva.application.inventario.kardex.MovimientoKardexResponse;
import pe.edu.upeu.moliselva.application.shared.cqrs.Query;

/** Movimientos del kardex de maíz en orden cronológico. Disponible para los cuatro roles. */
public record ListarKardexMaizQuery() implements Query<List<MovimientoKardexResponse>> {}
