package pe.edu.upeu.moliselva.application.formulas.usecase;

import pe.edu.upeu.moliselva.application.formulas.gateway.FormulaGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;

final class FormulaChecks {
    private FormulaChecks() {}

    static Formula existente(FormulaGateway formulas, long id) {
        return formulas.buscar(id).orElseThrow(() ->
            FallaAplicacion.noEncontrado("FORMULA_NO_ENCONTRADO","El registro no existe"));
    }

    static FormulaDetalle detalleExistente(FormulaGateway formulas, long id, long idInsumo) {
        return formulas.buscarDetalle(id,idInsumo).orElseThrow(() ->
            FallaAplicacion.noEncontrado("FORMULA_DETALLE_NO_ENCONTRADO","El registro no existe"));
    }

    /** Existe y todavía no la usa ningún lote. */
    static void editable(FormulaGateway formulas, long id) {
        existente(formulas,id);
        if (formulas.usadaPorLote(id)) {
            throw FallaAplicacion.conflicto("REGISTRO_NO_EDITABLE","La fórmula ya está utilizada por un lote");
        }
    }

    static void referenciaActiva(boolean activa) {
        if (!activa) {
            throw FallaAplicacion.datosInvalidos("REFERENCIA_INVALIDA","El registro relacionado no existe o está inactivo");
        }
    }
}
