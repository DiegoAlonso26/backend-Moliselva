package pe.edu.upeu.moliselva.application.formulas.usecase;

import java.math.BigDecimal;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;

/** Solo JEFE_PLANTA. Los ingredientes de una fórmula usada por un lote ya no se modifican. */
public interface GestionarDetallesFormulaUseCase {
    FormulaDetalle agregar(long id, long idInsumo, BigDecimal proporcion, long usuarioId);
    FormulaDetalle actualizar(long id, long insumoRuta, long idInsumo, BigDecimal proporcion, long usuarioId);
    void eliminar(long id, long idInsumo, long usuarioId);
}
