package pe.edu.upeu.moliselva.ventas.application;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.ventas.application.port.out.BitacoraVentaPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.DespachoPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.PedidoPort;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;
import pe.edu.upeu.moliselva.ventas.domain.EstadoDespacho;
import pe.edu.upeu.moliselva.ventas.domain.EstadoPedido;
import pe.edu.upeu.moliselva.ventas.domain.Pedido;
import pe.edu.upeu.moliselva.ventas.domain.TipoEntrega;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConfirmarDespachoTest {
    private final DespachoPort despachos=mock(DespachoPort.class);
    private final PedidoPort pedidos=mock(PedidoPort.class);
    private final BitacoraVentaPort bitacora=mock(BitacoraVentaPort.class);
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
