package pe.edu.upeu.moliselva.presentation.rest.inventario.mapper;

import pe.edu.upeu.moliselva.application.inventario.kardex.MovimientoKardexResponse;
import pe.edu.upeu.moliselva.application.inventario.kardex.stock.StockMaizResponse;
import pe.edu.upeu.moliselva.presentation.rest.inventario.dto.KardexMaizDto;

public final class KardexMaizMapper {
    private KardexMaizMapper() {}

    public static KardexMaizDto.Kardex aRespuesta(MovimientoKardexResponse m) {
        return new KardexMaizDto.Kardex(m.idMovimiento(),m.fechaHora(),m.tipoMovimiento(),
            m.cantidadKg(),m.saldoKg(),m.idRecepcion(),m.usuario());
    }

    public static KardexMaizDto.Stock aStock(StockMaizResponse stock) {
        return new KardexMaizDto.Stock(stock.stockKg());
    }
}
