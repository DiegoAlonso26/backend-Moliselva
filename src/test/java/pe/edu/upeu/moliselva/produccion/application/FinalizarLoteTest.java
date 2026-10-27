package pe.edu.upeu.moliselva.produccion.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.produccion.application.port.out.BitacoraProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.CatalogoProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.InventarioProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.LotePort;
import pe.edu.upeu.moliselva.produccion.domain.EstadoLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;
import pe.edu.upeu.moliselva.produccion.domain.LoteInsumo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FinalizarLoteTest {
    private final LotePort lotes=mock(LotePort.class);
    private final CatalogoProduccionPort catalogo=mock(CatalogoProduccionPort.class);
    private final InventarioProduccionPort inventario=mock(InventarioProduccionPort.class);
    private final BitacoraProduccionPort bitacora=mock(BitacoraProduccionPort.class);
    private final FinalizarLote caso=new FinalizarLote(lotes,catalogo,inventario,bitacora);

    @Test
    void utilizaCantidadRealCuandoExisteYCalculadaSiFalta() {
        when(lotes.bloquear(10)).thenReturn(Optional.of(lote(EstadoLote.EN_PROCESO)));
        when(lotes.listarInsumos(10)).thenReturn(List.of(
            new LoteInsumo(10,3,new BigDecimal("250"),new BigDecimal("249")),
            new LoteInsumo(10,4,new BigDecimal("50"),null)));
        when(catalogo.insumoActivo(3)).thenReturn(true);
        when(catalogo.insumoActivo(4)).thenReturn(true);
        when(lotes.buscar(10)).thenReturn(Optional.of(lote(EstadoLote.TERMINADO)));

        Lote resultado=caso.finalizar(10,2);

        assertEquals(EstadoLote.TERMINADO,resultado.estado());
        verify(inventario).registrarSalida(10,3,new BigDecimal("249"),2);
        verify(inventario).registrarSalida(10,4,new BigDecimal("50"),2);
        verify(inventario).registrarEntrada(10,7,500,2);
        verify(lotes).marcarTerminado(10,2);
        verify(bitacora).registrar(2,"FINALIZAR_LOTE id=10");
    }

    @Test
    void impideFinalizarSiFaltaUnIngredienteDeFormula() {
        when(lotes.bloquear(10)).thenReturn(Optional.of(lote(EstadoLote.EN_PROCESO)));
        when(lotes.listarInsumos(10)).thenReturn(List.of(
            new LoteInsumo(10,3,new BigDecimal("250"),null)));
        when(lotes.faltanInsumosFormula(10,5)).thenReturn(true);

        ProduccionFailure error=assertThrows(ProduccionFailure.class,() -> caso.finalizar(10,2));

        assertEquals("REGISTRO_NO_EDITABLE",error.codigo());
        verifyNoInteractions(inventario,bitacora);
        verify(lotes,never()).marcarTerminado(anyLong(),anyLong());
    }

    @Test
    void impideRepetirElCierre() {
        when(lotes.bloquear(10)).thenReturn(Optional.of(lote(EstadoLote.TERMINADO)));

        ProduccionFailure error=assertThrows(ProduccionFailure.class,() -> caso.finalizar(10,2));

        assertEquals(409,error.status());
        verifyNoInteractions(inventario,bitacora);
    }

    private static Lote lote(EstadoLote estado) {
        return new Lote(10,7,5,LocalDateTime.of(2026,10,27,12,0),500,estado,2);
    }
}
