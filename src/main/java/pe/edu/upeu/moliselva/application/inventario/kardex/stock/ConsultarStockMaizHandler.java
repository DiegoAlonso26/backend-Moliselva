package pe.edu.upeu.moliselva.application.inventario.kardex.stock;

import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizConsultaGateway;
import pe.edu.upeu.moliselva.application.inventario.kardex.MaizNoConfigurado;
import pe.edu.upeu.moliselva.application.shared.cqrs.QueryHandler;

public class ConsultarStockMaizHandler implements QueryHandler<ConsultarStockMaizQuery, StockMaizResponse> {
    private final KardexMaizConsultaGateway kardex;

    public ConsultarStockMaizHandler(KardexMaizConsultaGateway kardex) { this.kardex=kardex; }

    @Override
    public StockMaizResponse handle(ConsultarStockMaizQuery query) {
        return new StockMaizResponse(kardex.stock(kardex.maizId().orElseThrow(MaizNoConfigurado::new)));
    }
}
