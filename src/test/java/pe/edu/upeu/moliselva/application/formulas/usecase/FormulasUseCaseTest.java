package pe.edu.upeu.moliselva.application.formulas.usecase;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.application.formulas.gateway.FormulaGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;
import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDatos;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Proporciones ficticias de prueba; no corresponden a ninguna fórmula real. */
class FormulasUseCaseTest {
    private final FormulaGateway formulas = mock(FormulaGateway.class);
    private final BitacoraGateway bitacora = mock(BitacoraGateway.class);
    private final Formula formula = new Formula(5L,2L,"v1",true,1L,null);

    @Test
    void versionSeNormalizaYVigenteEsTrueSiNoSeEnvia() {
        var datos = FormulaDatos.de(2,"  v1 ",null);
        assertEquals("v1",datos.version());
        assertTrue(datos.vigente());
    }

    @Test
    void formulaUsadaPorUnLoteNoSeModificaNiSeElimina() {
        when(formulas.buscar(5)).thenReturn(Optional.of(formula));
        when(formulas.usadaPorLote(5)).thenReturn(true);
        var gestionar = new GestionarFormulas(formulas,bitacora);
        var detalles = new GestionarDetallesFormula(formulas,bitacora);

        for (FallaNegocio error : new FallaNegocio[]{
                assertThrows(FallaNegocio.class, () -> gestionar.actualizar(5,FormulaDatos.de(2,"v2",true),1)),
                assertThrows(FallaNegocio.class, () -> gestionar.eliminar(5,1)),
                assertThrows(FallaNegocio.class, () -> detalles.agregar(5,3,BigDecimal.TEN,1))}) {
            assertEquals("REGISTRO_NO_EDITABLE",error.codigo());
            assertEquals(FallaNegocio.Tipo.CONFLICTO,error.tipo());
        }
        verify(formulas,never()).actualizar(anyLong(),any());
        verify(formulas,never()).eliminar(anyLong());
        verifyNoInteractions(bitacora);
    }

    @Test
    void cambiarVigenciaSiguePermitidoConLotes() {
        when(formulas.buscar(5)).thenReturn(Optional.of(formula));
        when(formulas.cambiarVigencia(5,false)).thenReturn(formula);

        new GestionarFormulas(formulas,bitacora).cambiarVigencia(5,false,1);

        verify(formulas,never()).usadaPorLote(anyLong());
        verify(bitacora).registrar(1,"CAMBIAR_VIGENCIA_FORMULA id=5/false",null);
    }

    @Test
    void productoOInsumoInactivoEsReferenciaInvalida() {
        when(formulas.productoActivo(2)).thenReturn(false);
        var crear = assertThrows(FallaNegocio.class, () ->
            new GestionarFormulas(formulas,bitacora).crear(FormulaDatos.de(2,"v1",true),1));
        assertEquals("REFERENCIA_INVALIDA",crear.codigo());

        when(formulas.buscar(5)).thenReturn(Optional.of(formula));
        when(formulas.insumoActivo(3)).thenReturn(false);
        var agregar = assertThrows(FallaNegocio.class, () ->
            new GestionarDetallesFormula(formulas,bitacora).agregar(5,3,BigDecimal.ONE,1));
        assertEquals("REFERENCIA_INVALIDA",agregar.codigo());
        verify(formulas,never()).agregarDetalle(anyLong(),anyLong(),any());
    }

    @Test
    void insumoDelCuerpoDebeCoincidirConLaRuta() {
        var error = assertThrows(FallaNegocio.class, () ->
            new GestionarDetallesFormula(formulas,bitacora).actualizar(5,3,4,BigDecimal.ONE,1));
        assertEquals("DATOS_INVALIDOS",error.codigo());
        verifyNoInteractions(formulas,bitacora);
    }

    @Test
    void detalleInexistenteDevuelveCodigoPropio() {
        when(formulas.buscarDetalle(5,3)).thenReturn(Optional.empty());
        var error = assertThrows(FallaNegocio.class, () -> new ConsultarFormulas(formulas).obtenerDetalle(5,3));
        assertEquals("FORMULA_DETALLE_NO_ENCONTRADO",error.codigo());

        when(formulas.buscarDetalle(5,4)).thenReturn(Optional.of(new FormulaDetalle(5L,4L,BigDecimal.ONE)));
        assertEquals(4L,new ConsultarFormulas(formulas).obtenerDetalle(5,4).idInsumo());
    }
}
