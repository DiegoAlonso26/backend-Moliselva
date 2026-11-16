package pe.edu.upeu.moliselva.application.formulas.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;

/** Solo JEFE_PLANTA (se exige en la presentación); las proporciones son confidenciales. */
public interface ConsultarFormulasUseCase {
    List<Formula> listar();
    Formula obtener(long id);
    List<FormulaDetalle> listarDetalles(long id);
    FormulaDetalle obtenerDetalle(long id, long idInsumo);
}
