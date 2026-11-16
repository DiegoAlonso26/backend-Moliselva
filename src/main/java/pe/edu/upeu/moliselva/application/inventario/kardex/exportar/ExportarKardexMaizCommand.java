package pe.edu.upeu.moliselva.application.inventario.kardex.exportar;

import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.cqrs.Command;

/**
 * Exportar el kardex de maíz a CSV. Es un command y no una query porque deja la
 * exportación registrada en la bitácora. Solo ADMINISTRADOR, JEFE_PLANTA o CONTADORA.
 */
public record ExportarKardexMaizCommand(Actor actor) implements Command<ExportarKardexMaizResponse> {}
