package pe.edu.upeu.moliselva.application.inventario.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.inventario.ItemStock;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoInventario;

/** Stock de insumos y productos y movimientos del kardex general. Los cuatro roles. */
public interface ConsultarInventarioUseCase {
    List<ItemStock> stockTodos();
    ItemStock stockItem(String tipo, long id);
    List<MovimientoInventario> movimientos();
    MovimientoInventario movimiento(long id);
}
