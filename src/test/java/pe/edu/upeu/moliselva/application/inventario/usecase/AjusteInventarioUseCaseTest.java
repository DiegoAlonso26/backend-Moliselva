package pe.edu.upeu.moliselva.application.inventario.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pe.edu.upeu.moliselva.application.inventario.gateway.InventarioGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;
import pe.edu.upeu.moliselva.domain.inventario.AjusteInventario;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoInventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AjusteInventarioUseCaseTest {
    private final InventarioGateway inventario = mock(InventarioGateway.class);
    private final BitacoraGateway bitacora = mock(BitacoraGateway.class);
    private final RegistrarAjuste ajustes = new RegistrarAjuste(inventario,bitacora);

    @ParameterizedTest(name = "insumo={0}, producto={1}")
    @CsvSource(value = {"NULL,NULL", "1,2", "0,NULL", "NULL,-3"}, nullValues = "NULL")
    void exigeExactamenteUnInsumoOUnProductoPositivo(Long insumo, Long producto) {
        var error = assertThrows(FallaNegocio.class, () ->
            ajustes.ajustar(new AjusteInventario("AJUSTE_ENTRADA",insumo,producto,BigDecimal.ONE,"Conteo"),2));

        assertEquals("ITEM_INVALIDO",error.codigo());
        assertEquals(FallaNegocio.Tipo.DATOS_INVALIDOS,error.tipo());
        verifyNoInteractions(inventario,bitacora);
    }

    @Test
    void itemInactivoNoSeAjusta() {
        when(inventario.itemActivo(4L,null)).thenReturn(false);

        var error = assertThrows(FallaNegocio.class, () ->
            ajustes.ajustar(new AjusteInventario("AJUSTE_SALIDA",4L,null,BigDecimal.ONE,"Merma"),2));

        assertEquals("ITEM_INVALIDO",error.codigo());
        verify(inventario,never()).insertarAjuste(any(),anyString(),anyLong());
    }

    @Test
    void ajusteValidoUsaLaUnidadDelInsumoYRegistraElMotivoSinEspacios() {
        var ajuste = new AjusteInventario("AJUSTE_ENTRADA",4L,null,new BigDecimal("2.5"),"  Conteo físico ");
        when(inventario.itemActivo(4L,null)).thenReturn(true);
        when(inventario.unidadMedida(4L)).thenReturn("kg");
        when(inventario.insertarAjuste(ajuste,"kg",2)).thenReturn(30L);
        when(inventario.movimiento(30)).thenReturn(Optional.of(new MovimientoInventario(30,LocalDateTime.now(),
            "AJUSTE_ENTRADA",4L,null,new BigDecimal("2.5"),"kg",null,null,null,"jefe")));

        assertEquals(30,ajustes.ajustar(ajuste,2).idMovimiento());
        verify(bitacora).registrar(2,"AJUSTE_INVENTARIO id=30","Conteo físico");
    }

    @Test
    void stockInsuficienteDelGatewayNoDejaBitacora() {
        var ajuste = new AjusteInventario("AJUSTE_SALIDA",null,7L,new BigDecimal("999"),"Salida");
        when(inventario.itemActivo(null,7L)).thenReturn(true);
        when(inventario.unidadMedida(null)).thenReturn("kg");
        when(inventario.insertarAjuste(ajuste,"kg",2)).thenThrow(
            pe.edu.upeu.moliselva.application.shared.FallaAplicacion.conflicto("STOCK_INSUFICIENTE","El ajuste dejaría stock negativo"));

        var error = assertThrows(FallaNegocio.class, () -> ajustes.ajustar(ajuste,2));

        assertEquals("STOCK_INSUFICIENTE",error.codigo());
        verifyNoInteractions(bitacora);
    }

    @Test
    void tipoDeItemInvalidoEnConsultaDeStock() {
        var error = assertThrows(FallaNegocio.class, () -> new ConsultarInventario(inventario).stockItem("LOTE",1));
        assertEquals("TIPO_ITEM_INVALIDO",error.codigo());
        verifyNoInteractions(inventario);
    }
}
