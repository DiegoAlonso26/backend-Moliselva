package pe.edu.upeu.moliselva.application.inventario.kardex.listar;

import java.util.List;
import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizConsultaGateway;
import pe.edu.upeu.moliselva.application.inventario.kardex.MaizNoConfigurado;
import pe.edu.upeu.moliselva.application.inventario.kardex.MovimientoKardexResponse;
import pe.edu.upeu.moliselva.application.shared.cqrs.QueryHandler;

public class ListarKardexMaizHandler implements QueryHandler<ListarKardexMaizQuery, List<MovimientoKardexResponse>> {
    private final KardexMaizConsultaGateway kardex;

    public ListarKardexMaizHandler(KardexMaizConsultaGateway kardex) { this.kardex=kardex; }

    @Override
    public List<MovimientoKardexResponse> handle(ListarKardexMaizQuery query) {
        return kardex.movimientos(kardex.maizId().orElseThrow(MaizNoConfigurado::new));
    }
}
