package pe.edu.upeu.moliselva.application.recepcion.listar;

import java.util.List;
import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionConsultaGateway;
import pe.edu.upeu.moliselva.application.shared.cqrs.QueryHandler;

public class ListarRecepcionesHandler implements QueryHandler<ListarRecepcionesQuery, List<RecepcionResponse>> {
    private final RecepcionConsultaGateway consultas;

    public ListarRecepcionesHandler(RecepcionConsultaGateway consultas) { this.consultas=consultas; }

    @Override
    public List<RecepcionResponse> handle(ListarRecepcionesQuery query) {
        return consultas.listar(query.fecha());
    }
}
