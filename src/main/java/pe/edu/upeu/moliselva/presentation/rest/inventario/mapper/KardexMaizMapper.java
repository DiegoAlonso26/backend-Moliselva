package pe.edu.upeu.moliselva.presentation.rest.inventario.mapper;

import java.math.BigDecimal;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoKardex;
import pe.edu.upeu.moliselva.presentation.rest.inventario.dto.KardexMaizDto;

public final class KardexMaizMapper {
    private KardexMaizMapper() {}

    public static KardexMaizDto.Kardex aRespuesta(MovimientoKardex m) {
        return new KardexMaizDto.Kardex(m.idMovimiento(),m.fechaHora(),m.tipoMovimiento(),
            m.cantidadKg(),m.saldoKg(),m.idRecepcion(),m.usuario());
    }

    public static KardexMaizDto.Stock aStock(BigDecimal stockKg) {
        return new KardexMaizDto.Stock(stockKg);
    }
}
