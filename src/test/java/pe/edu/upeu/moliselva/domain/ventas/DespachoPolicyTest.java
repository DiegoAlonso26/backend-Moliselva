package pe.edu.upeu.moliselva.domain.ventas;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas de pedidos y despachos: qué pedido admite despacho o cancelación, y destino del reparto. */
class DespachoPolicyTest {

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"PENDIENTE", "EN_PREPARACION"})
    void pedidoAbiertoAdmiteDespachoYCancelacion(EstadoPedido estado) {
        assertTrue(DespachoPolicy.pedidoAdmiteDespacho(pedido(estado)));
        assertTrue(DespachoPolicy.pedidoCancelable(pedido(estado)));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"ATENDIDO", "CANCELADO"})
    void pedidoCerradoNoAdmiteDespachoNiCancelacion(EstadoPedido estado) {
        assertFalse(DespachoPolicy.pedidoAdmiteDespacho(pedido(estado)));
        assertFalse(DespachoPolicy.pedidoCancelable(pedido(estado)));
    }

    @Test
    void repartoExigeDestinoNoVacio() {
        assertTrue(DespachoPolicy.requiereDestino(new DatosDespacho(1, TipoEntrega.REPARTO, null)));
        assertTrue(DespachoPolicy.requiereDestino(new DatosDespacho(1, TipoEntrega.REPARTO, "   ")));
        assertFalse(DespachoPolicy.requiereDestino(new DatosDespacho(1, TipoEntrega.REPARTO, "Av. Principal 123")));
    }

    @Test
    void recojoEnPlantaNoExigeDestino() {
        assertFalse(DespachoPolicy.requiereDestino(new DatosDespacho(1, TipoEntrega.RECOJO_PLANTA, null)));
    }

    @Test
    void destinoSeNormaliza() {
        assertEquals("Av. Principal 123", new DatosDespacho(1, TipoEntrega.REPARTO, "  Av. Principal 123 ").destinoNormalizado());
        assertNull(new DatosDespacho(1, TipoEntrega.REPARTO, "").destinoNormalizado());
    }

    private static Pedido pedido(EstadoPedido estado) {
        return new Pedido(1, 2, LocalDateTime.of(2026, 11, 16, 8, 0), estado, null);
    }
}
