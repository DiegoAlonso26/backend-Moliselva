package pe.edu.upeu.moliselva.application.inventario.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;

/** Exportación del kardex de maíz a CSV. Solo ADMINISTRADOR, JEFE_PLANTA o CONTADORA; queda en la bitácora. */
public interface ExportarKardexMaizUseCase {
    byte[] exportar(Actor actor);
}
