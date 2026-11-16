package pe.edu.upeu.moliselva.domain.formulas;

import java.math.BigDecimal;

/** Ingrediente de una fórmula. La proporción es confidencial: no se publica ni se registra fuera de la base. */
public record FormulaDetalle(Long idFormula, Long idInsumo, BigDecimal proporcionConfidencial) {}
