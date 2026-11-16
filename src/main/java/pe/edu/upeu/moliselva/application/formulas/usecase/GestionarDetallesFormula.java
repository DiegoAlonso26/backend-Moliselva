package pe.edu.upeu.moliselva.application.formulas.usecase;

import java.math.BigDecimal;
import pe.edu.upeu.moliselva.application.formulas.gateway.FormulaGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;

public class GestionarDetallesFormula implements GestionarDetallesFormulaUseCase {
    private final FormulaGateway formulas;
    private final BitacoraGateway bitacora;

    public GestionarDetallesFormula(FormulaGateway formulas, BitacoraGateway bitacora) {
        this.formulas=formulas; this.bitacora=bitacora;
    }

    @Override
    public FormulaDetalle agregar(long id, long idInsumo, BigDecimal proporcion, long usuarioId) {
        FormulaChecks.editable(formulas,id);
        FormulaChecks.referenciaActiva(formulas.insumoActivo(idInsumo));
        var detalle=formulas.agregarDetalle(id,idInsumo,proporcion);
        bitacora.registrar(usuarioId,"CREAR_FORMULA_DETALLE id="+id+"/"+idInsumo,null);
        return detalle;
    }

    @Override
    public FormulaDetalle actualizar(long id, long insumoRuta, long idInsumo, BigDecimal proporcion, long usuarioId) {
        if (idInsumo != insumoRuta) {
            throw FallaAplicacion.datosInvalidos("DATOS_INVALIDOS","El idInsumo del cuerpo debe coincidir con la ruta");
        }
        FormulaChecks.editable(formulas,id);
        FormulaChecks.detalleExistente(formulas,id,insumoRuta);
        var detalle=formulas.actualizarDetalle(id,insumoRuta,proporcion);
        bitacora.registrar(usuarioId,"ACTUALIZAR_FORMULA_DETALLE id="+id+"/"+insumoRuta,null);
        return detalle;
    }

    @Override
    public void eliminar(long id, long idInsumo, long usuarioId) {
        FormulaChecks.editable(formulas,id);
        FormulaChecks.detalleExistente(formulas,id,idInsumo);
        formulas.eliminarDetalle(id,idInsumo);
        bitacora.registrar(usuarioId,"ELIMINAR_FORMULA_DETALLE id="+id+"/"+idInsumo,null);
    }
}
