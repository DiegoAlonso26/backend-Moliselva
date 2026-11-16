package pe.edu.upeu.moliselva.application.inventario.gateway;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoKardex;

public interface KardexMaizGateway {
    Optional<Long> maizId();
    List<MovimientoKardex> movimientos(long idMaiz);
    BigDecimal stock(long idMaiz);
}
