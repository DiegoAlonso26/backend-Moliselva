package pe.edu.upeu.moliselva.application.formulas.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.formulas.gateway.FormulaGateway;
import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;

public class ConsultarFormulas implements ConsultarFormulasUseCase {
    private final FormulaGateway formulas;

    public ConsultarFormulas(FormulaGateway formulas) { this.formulas=formulas; }

    @Override public List<Formula> listar() { return formulas.listar(); }

    @Override public Formula obtener(long id) { return FormulaChecks.existente(formulas,id); }

    @Override public List<FormulaDetalle> listarDetalles(long id) {
        FormulaChecks.existente(formulas,id);
        return formulas.listarDetalles(id);
    }

    @Override public FormulaDetalle obtenerDetalle(long id, long idInsumo) {
        return FormulaChecks.detalleExistente(formulas,id,idInsumo);
    }
}
