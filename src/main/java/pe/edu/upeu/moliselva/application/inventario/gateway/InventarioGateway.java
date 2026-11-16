package pe.edu.upeu.moliselva.application.inventario.gateway;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.inventario.AjusteInventario;
import pe.edu.upeu.moliselva.domain.inventario.ItemStock;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoInventario;

public interface InventarioGateway {
    List<ItemStock> stockTodos();
    Optional<ItemStock> stockItem(String tipo, long id);
    List<MovimientoInventario> movimientos();
    Optional<MovimientoInventario> movimiento(long id);
    boolean itemActivo(Long idInsumo, Long idProducto);
    /** Unidad del insumo; los productos se registran en kg. */
    String unidadMedida(Long idInsumo);
    /** Lanza STOCK_INSUFICIENTE (409) si la base rechaza el ajuste por dejar stock negativo. */
    long insertarAjuste(AjusteInventario ajuste, String unidad, long usuarioId);
}
