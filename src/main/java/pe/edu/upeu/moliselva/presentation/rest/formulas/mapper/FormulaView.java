package pe.edu.upeu.moliselva.presentation.rest.formulas.mapper;

import java.util.LinkedHashMap;
import java.util.Map;
import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;
import pe.edu.upeu.moliselva.presentation.rest.formulas.dto.OperacionDto;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDatos;

/** JSON de fórmulas: mismas claves y orden que las columnas de formula y formula_detalle. */
public final class FormulaView {
    private FormulaView() {}

    public static Map<String,Object> from(Formula f) {
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("idFormula",f.idFormula());
        out.put("idProducto",f.idProducto());
        out.put("version",f.version());
        out.put("vigente",f.vigente());
        out.put("idCreador",f.idCreador());
        out.put("fechaCreacion",f.fechaCreacion());
        return out;
    }

    public static Map<String,Object> from(FormulaDetalle d) {
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("idFormula",d.idFormula());
        out.put("idInsumo",d.idInsumo());
        out.put("proporcionConfidencial",d.proporcionConfidencial());
        return out;
    }

    public static FormulaDatos aDatos(OperacionDto.Formula input) {
        return FormulaDatos.de(input.idProducto(),input.version(),input.vigente());
    }
}
