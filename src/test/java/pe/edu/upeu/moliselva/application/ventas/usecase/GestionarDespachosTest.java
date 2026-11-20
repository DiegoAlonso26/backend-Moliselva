package pe.edu.upeu.moliselva.application.ventas.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.application.ventas.gateway.*;
import pe.edu.upeu.moliselva.domain.ventas.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestionarDespachosTest {
    private final DespachoGateway despachos=mock(DespachoGateway.class);
    private final PedidoGateway pedidos=mock(PedidoGateway.class);
    private final BitacoraVentaGateway bitacora=mock(BitacoraVentaGateway.class);
    private final GestionarDespachos caso=new GestionarDespachos(despachos,pedidos,bitacora);
    private final DatosDespacho datos=new DatosDespacho(5,TipoEntrega.RECOJO_PLANTA,null);

    private void pedido() {
        when(pedidos.bloquear(5)).thenReturn(Optional.of(new Pedido(5,3,
            LocalDateTime.of(2026,11,20,12,0),EstadoPedido.PENDIENTE,null)));
    }

    @Test void copiaTodasLasLineasSinMoverStockNiCambiarPedido() {
        pedido();
        when(pedidos.listarDetalles(5)).thenReturn(List.of(
            new PedidoDetalle(5,11,3,new BigDecimal("120.00")),
            new PedidoDetalle(5,12,7,new BigDecimal("280.00"))));
        var borrador=new Despacho(8,5,LocalDateTime.of(2026,11,20,12,0),
            TipoEntrega.RECOJO_PLANTA,null,EstadoDespacho.BORRADOR,2);
        when(despachos.crear(datos,2)).thenReturn(borrador);
        assertSame(borrador,caso.crear(datos,2));
        verify(despachos).agregarDetalle(8,new DatosDetalle(11,3,new BigDecimal("120.00")));
        verify(despachos).agregarDetalle(8,new DatosDetalle(12,7,new BigDecimal("280.00")));
        verify(despachos).crear(datos,2);
        verifyNoMoreInteractions(despachos);
        verify(pedidos).bloquear(5);
        verify(pedidos).listarDetalles(5);
        verifyNoMoreInteractions(pedidos);
        verify(bitacora).registrar(2,"CREAR_DESPACHO id=8");
    }

    @Test void rechazaPedidoVacioAntesDeCrearCabecera() {
        pedido();
        when(pedidos.listarDetalles(5)).thenReturn(List.of());
        assertEquals("PEDIDO_SIN_DETALLES",assertThrows(VentaFailure.class,
            () -> caso.crear(datos,2)).codigo());
        verifyNoInteractions(despachos,bitacora);
    }

    @Test void noRegistraExitoSiFallaCopiarUnaLinea() {
        pedido();
        when(pedidos.listarDetalles(5)).thenReturn(List.of(
            new PedidoDetalle(5,11,3,new BigDecimal("120.00"))));
        when(despachos.crear(datos,2)).thenReturn(new Despacho(8,5,
            LocalDateTime.of(2026,11,20,12,0),TipoEntrega.RECOJO_PLANTA,null,
            EstadoDespacho.BORRADOR,2));
        when(despachos.agregarDetalle(eq(8L),any())).thenThrow(new IllegalStateException("synthetic"));
        assertThrows(IllegalStateException.class,() -> caso.crear(datos,2));
        verifyNoInteractions(bitacora);
        verify(despachos,never()).confirmarEnBase(anyLong(),anyLong());
    }
}
