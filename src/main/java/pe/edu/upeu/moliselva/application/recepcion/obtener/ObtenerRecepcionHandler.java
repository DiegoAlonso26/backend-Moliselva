package pe.edu.upeu.moliselva.application.recepcion.obtener;

import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.recepcion.ReglasRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionConsultaGateway;
import pe.edu.upeu.moliselva.application.shared.cqrs.QueryHandler;

public class ObtenerRecepcionHandler implements QueryHandler<ObtenerRecepcionQuery, RecepcionResponse> {
    private final RecepcionConsultaGateway consultas;

    public ObtenerRecepcionHandler(RecepcionConsultaGateway consultas) { this.consultas=consultas; }

    @Override
    public RecepcionResponse handle(ObtenerRecepcionQuery query) {
        return consultas.buscar(query.idRecepcion()).orElseThrow(ReglasRecepcion::noEncontrada);
    }
}
