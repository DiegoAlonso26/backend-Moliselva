package pe.edu.upeu.moliselva.application.maestros.gateway;

import java.math.BigDecimal;
import java.util.Optional;

/** Stock actual de un insumo o producto, para impedir desactivarlo si aún tiene existencias. */
public interface StockItemGateway {
    Optional<BigDecimal> stockActual(String tipo, long id);
}
