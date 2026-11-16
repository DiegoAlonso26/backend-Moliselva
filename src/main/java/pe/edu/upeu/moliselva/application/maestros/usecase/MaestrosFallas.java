package pe.edu.upeu.moliselva.application.maestros.usecase;

import java.math.BigDecimal;
import pe.edu.upeu.moliselva.application.maestros.gateway.StockItemGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;

final class MaestrosFallas {
    private MaestrosFallas() {}

    static FallaAplicacion noEncontrado(String entidad) {
        return FallaAplicacion.noEncontrado(entidad+"_NO_ENCONTRADO","El registro no existe o está inactivo");
    }

    static void sinStock(StockItemGateway stock, String tipo, long id) {
        if (stock.stockActual(tipo,id).map(BigDecimal::signum).map(signo -> signo != 0).orElse(false)) {
            throw FallaAplicacion.conflicto("ITEM_CON_STOCK",
                "El insumo o producto tiene stock; registre su salida antes de desactivarlo");
        }
    }
}
