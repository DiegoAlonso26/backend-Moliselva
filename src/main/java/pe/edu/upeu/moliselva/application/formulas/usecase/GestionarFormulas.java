package pe.edu.upeu.moliselva.application.formulas.usecase;

import pe.edu.upeu.moliselva.application.formulas.gateway.FormulaGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDatos;

public class GestionarFormulas implements GestionarFormulasUseCase {
    private final FormulaGateway formulas;
    private final BitacoraGateway bitacora;

    public GestionarFormulas(FormulaGateway formulas, BitacoraGateway bitacora) {
        this.formulas=formulas; this.bitacora=bitacora;
    }

    @Override
    public Formula crear(FormulaDatos datos, long usuarioId) {
        FormulaChecks.referenciaActiva(formulas.productoActivo(datos.idProducto()));
        var formula=formulas.crear(datos,usuarioId);
        bitacora.registrar(usuarioId,"CREAR_FORMULA id="+formula.idFormula(),null);
        return formula;
    }

    @Override
    public Formula actualizar(long id, FormulaDatos datos, long usuarioId) {
        FormulaChecks.editable(formulas,id);
        FormulaChecks.referenciaActiva(formulas.productoActivo(datos.idProducto()));
        var formula=formulas.actualizar(id,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_FORMULA id="+id,null);
        return formula;
    }

    @Override
    public void eliminar(long id, long usuarioId) {
        FormulaChecks.editable(formulas,id);
        formulas.eliminar(id);
        bitacora.registrar(usuarioId,"ELIMINAR_FORMULA id="+id,null);
    }

    @Override
    public Formula cambiarVigencia(long id, boolean vigente, long usuarioId) {
        FormulaChecks.existente(formulas,id);
        var formula=formulas.cambiarVigencia(id,vigente);
        bitacora.registrar(usuarioId,"CAMBIAR_VIGENCIA_FORMULA id="+id+"/"+vigente,null);
        return formula;
    }
}
