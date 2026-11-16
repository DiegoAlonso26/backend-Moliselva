package pe.edu.upeu.moliselva.application.formulas.usecase;

import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDatos;

/** Solo JEFE_PLANTA. Una fórmula usada por un lote ya no se modifica ni se elimina. */
public interface GestionarFormulasUseCase {
    Formula crear(FormulaDatos datos, long usuarioId);
    Formula actualizar(long id, FormulaDatos datos, long usuarioId);
    void eliminar(long id, long usuarioId);
    Formula cambiarVigencia(long id, boolean vigente, long usuarioId);
}
