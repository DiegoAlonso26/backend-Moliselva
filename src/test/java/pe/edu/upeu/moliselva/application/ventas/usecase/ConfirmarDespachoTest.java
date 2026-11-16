package pe.edu.upeu.moliselva.application.ventas.usecase;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.DespachoGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.PedidoGateway;
import pe.edu.upeu.moliselva.domain.ventas.Despacho;
import pe.edu.upeu.moliselva.domain.ventas.EstadoDespacho;
import pe.edu.upeu.moliselva.domain.ventas.EstadoPedido;
import pe.edu.upeu.moliselva.domain.ventas.Pedido;
import pe.edu.upeu.moliselva.domain.ventas.TipoEntrega;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConfirmarDespachoTest {
    private final DespachoGateway despachos=mock(DespachoGateway.class);
    private final PedidoGateway pedidos=mock(PedidoGateway.class);
    private final BitacoraVentaGateway bitacora=mock(BitacoraVentaGateway.class);
    private final ConfirmarDespacho caso=new ConfirmarDespacho(despachos,pedidos,bitacora);

    @Test
    void confirmaUnaVezDespuesDeValidarPedidoYCantidades() {
        when(despachos.bloquear(8)).thenReturn(Optional.of(despacho(EstadoDespacho.BORRADOR)));
        when(pedidos.bloquear(5)).thenReturn(Optional.of(pedido(EstadoPedido.EN_PREPARACION)));
        when(despachos.buscar(8)).thenReturn(Optional.of(despacho(EstadoDespacho.CONFIRMADO)));

        Despacho resultado=caso.confirmar(8,2);

        assertEquals(EstadoDespacho.CONFIRMADO,resultado.estado());
        verify(despachos).excedePedido(5,8);
        verify(despachos).confirmarEnBase(8,2);
        verify(bitacora).registrar(2,"CONFIRMAR_DESPACHO id=8");
    }

    @Test
    void rechazaCantidadesQueExcedenPedidoAntesDeMoverStock() {
        when(despachos.bloquear(8)).thenReturn(Optional.of(despacho(EstadoDespacho.BORRADOR)));
        when(pedidos.bloquear(5)).thenReturn(Optional.of(pedido(EstadoPedido.PENDIENTE)));
        when(despachos.excedePedido(5,8)).thenReturn(true);

        VentaFailure error=assertThrows(VentaFailure.class,() -> caso.confirmar(8,2));

        assertEquals("REGISTRO_NO_EDITABLE",error.codigo());
        verify(despachos,never()).confirmarEnBase(anyLong(),anyLong());
        verifyNoInteractions(bitacora);
    }

    @Test
    void segundoBorradorNoConfirmaUnPedidoAtendido() {
        when(despachos.bloquear(8)).thenReturn(Optional.of(despacho(EstadoDespacho.BORRADOR)));
        when(pedidos.bloquear(5)).thenReturn(Optional.of(pedido(EstadoPedido.ATENDIDO)));

        VentaFailure error=assertThrows(VentaFailure.class,() -> caso.confirmar(8,2));

        assertEquals(VentaFailure.Type.CONFLICTO,error.type());
        verify(despachos,never()).confirmarEnBase(anyLong(),anyLong());
        verifyNoInteractions(bitacora);
    }

    private static Despacho despacho(EstadoDespacho estado) {
        return new Despacho(8,5,LocalDateTime.of(2026,10,27,12,0),
            TipoEntrega.REPARTO,"Lima",estado,2);
    }
    private static Pedido pedido(EstadoPedido estado) {
        return new Pedido(5,3,LocalDateTime.of(2026,10,27,12,0),estado,null);
    }
}
