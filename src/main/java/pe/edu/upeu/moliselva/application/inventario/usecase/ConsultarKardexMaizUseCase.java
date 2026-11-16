package pe.edu.upeu.moliselva.application.inventario.usecase;

import java.math.BigDecimal;
import java.util.List;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoKardex;

/** Consulta del kardex y del stock de maíz. Disponible para los cuatro roles. */
public interface ConsultarKardexMaizUseCase {
    List<MovimientoKardex> movimientos();
    BigDecimal stockKg();
}
