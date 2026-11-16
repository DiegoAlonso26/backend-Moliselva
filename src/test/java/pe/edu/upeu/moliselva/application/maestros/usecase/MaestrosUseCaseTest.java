package pe.edu.upeu.moliselva.application.maestros.usecase;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.application.maestros.gateway.ClienteGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.InsumoGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.ProductoGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.StockItemGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;
import pe.edu.upeu.moliselva.domain.maestros.ClienteDatos;
import pe.edu.upeu.moliselva.domain.maestros.Insumo;
import pe.edu.upeu.moliselva.domain.maestros.InsumoDatos;
import pe.edu.upeu.moliselva.domain.maestros.ProductoDatos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class MaestrosUseCaseTest {
    private final ClienteGateway clientes = mock(ClienteGateway.class);
    private final InsumoGateway insumos = mock(InsumoGateway.class);
    private final ProductoGateway productos = mock(ProductoGateway.class);
    private final StockItemGateway stock = mock(StockItemGateway.class);
    private final BitacoraGateway bitacora = mock(BitacoraGateway.class);

    @Test
    void datosSeNormalizanConLosMismosValoresPorDefecto() {
        var insumo = new InsumoDatos("  Sorgo ", " ");
        var producto = new ProductoDatos(" Balanceado ", "", "  Crecimiento ", "GJA", null);
        var cliente = new ClienteDatos("REVENDEDOR", " Cliente ", " ", "987", null);

        assertEquals("Sorgo", insumo.nombre());
        assertEquals("kg", insumo.unidadMedida());
        assertEquals("Balanceado", producto.nombre());
        assertNull(producto.especie());
        assertEquals("Crecimiento", producto.fase());
        assertEquals("Saco de 40 kg", producto.presentacion());
        assertEquals("Cliente", cliente.nombreRazonSocial());
        assertNull(cliente.documento());
        assertEquals("987", cliente.telefono());
    }

    @Test
    void insumoMaizNoSeModificaNiSeDesactiva() {
        when(insumos.buscarActivo(1)).thenReturn(Optional.of(new Insumo(1,"Maíz","kg")));
        var gestionar = new GestionarInsumos(insumos,stock,bitacora);

        var actualizar = assertThrows(FallaNegocio.class, () -> gestionar.actualizar(1,new InsumoDatos("Otro","kg"),2));
        var desactivar = assertThrows(FallaNegocio.class, () -> gestionar.desactivar(1,2));

        assertEquals("INSUMO_SISTEMA",actualizar.codigo());
        assertEquals("INSUMO_SISTEMA",desactivar.codigo());
        verify(insumos,never()).actualizar(anyLong(),any());
        verify(insumos,never()).desactivar(anyLong());
        verifyNoInteractions(stock,bitacora);
    }

    @Test
    void insumoConStockNoSeDesactivaYSeCompruebaAntesQueElUso() {
        when(insumos.buscarActivo(5)).thenReturn(Optional.of(new Insumo(5,"Sorgo","kg")));
        when(stock.stockActual("INSUMO",5)).thenReturn(Optional.of(new BigDecimal("12.000")));

        var error = assertThrows(FallaNegocio.class, () -> new GestionarInsumos(insumos,stock,bitacora).desactivar(5,2));

        assertEquals("ITEM_CON_STOCK",error.codigo());
        assertEquals(FallaNegocio.Tipo.CONFLICTO,error.tipo());
        verify(insumos,never()).enUso(anyLong());
        verifyNoInteractions(bitacora);
    }

    @Test
    void insumoEnUsoNoSeDesactiva() {
        when(insumos.buscarActivo(5)).thenReturn(Optional.of(new Insumo(5,"Sorgo","kg")));
        when(stock.stockActual("INSUMO",5)).thenReturn(Optional.of(BigDecimal.ZERO));
        when(insumos.enUso(5)).thenReturn(true);

        var error = assertThrows(FallaNegocio.class, () -> new GestionarInsumos(insumos,stock,bitacora).desactivar(5,2));

        assertEquals("INSUMO_EN_USO",error.codigo());
        verify(insumos,never()).desactivar(anyLong());
    }

    @Test
    void productoConStockOEnUsoNoSeDesactiva() {
        var gestionar = new GestionarProductos(productos,stock,bitacora);
        when(stock.stockActual("PRODUCTO",7)).thenReturn(Optional.of(new BigDecimal("40")));
        assertEquals("ITEM_CON_STOCK",assertThrows(FallaNegocio.class, () -> gestionar.desactivar(7,2)).codigo());

        when(stock.stockActual("PRODUCTO",8)).thenReturn(Optional.empty());
        when(productos.enUso(8)).thenReturn(true);
        assertEquals("PRODUCTO_EN_USO",assertThrows(FallaNegocio.class, () -> gestionar.desactivar(8,2)).codigo());
        verify(productos,never()).desactivar(anyLong());
    }

    @Test
    void clienteConPedidosAbiertosNoSeDesactivaYElDesactivadoQuedaEnBitacora() {
        var gestionar = new GestionarClientes(clientes,bitacora);
        when(clientes.tienePedidosAbiertos(3)).thenReturn(true);
        assertEquals("CLIENTE_CON_PEDIDOS",assertThrows(FallaNegocio.class, () -> gestionar.desactivar(3,2)).codigo());
        verify(clientes,never()).desactivar(3);

        when(clientes.desactivar(4)).thenReturn(true);
        gestionar.desactivar(4,2);
        verify(bitacora).registrar(2,"DESACTIVAR_CLIENTE id=4",null);
    }

    @Test
    void registroInexistenteDevuelveCodigoDeLaEntidad() {
        when(clientes.desactivar(9)).thenReturn(false);
        var error = assertThrows(FallaNegocio.class, () -> new GestionarClientes(clientes,bitacora).desactivar(9,2));
        assertEquals("CLIENTE_NO_ENCONTRADO",error.codigo());
        assertEquals(FallaNegocio.Tipo.NO_ENCONTRADO,error.tipo());
    }
}
