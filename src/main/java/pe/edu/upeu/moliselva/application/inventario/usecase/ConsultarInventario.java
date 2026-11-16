package pe.edu.upeu.moliselva.application.inventario.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.inventario.gateway.InventarioGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.inventario.ItemStock;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoInventario;

public class ConsultarInventario implements ConsultarInventarioUseCase {
    private final InventarioGateway inventario;

    public ConsultarInventario(InventarioGateway inventario) { this.inventario=inventario; }

    @Override
    public List<ItemStock> stockTodos() { return inventario.stockTodos(); }

    @Override
    public ItemStock stockItem(String tipo, long id) {
        if (!ItemStock.tipoValido(tipo)) {
            throw FallaAplicacion.datosInvalidos("TIPO_ITEM_INVALIDO","Use INSUMO o PRODUCTO");
        }
        return inventario.stockItem(tipo,id).orElseThrow(() ->
            FallaAplicacion.noEncontrado("ITEM_NO_ENCONTRADO","El item no existe"));
    }

    @Override
    public List<MovimientoInventario> movimientos() { return inventario.movimientos(); }

    @Override
    public MovimientoInventario movimiento(long id) {
        return inventario.movimiento(id).orElseThrow(() ->
            FallaAplicacion.noEncontrado("MOVIMIENTO_NO_ENCONTRADO","El movimiento no existe"));
    }
}
