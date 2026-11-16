package pe.edu.upeu.moliselva.application.recepcion.usecase;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;

public class ConsultarRecepcion implements ConsultarRecepcionUseCase {
    private final RecepcionGateway recepciones;
    public ConsultarRecepcion(RecepcionGateway recepciones) { this.recepciones=recepciones; }

    @Override
    public List<Recepcion> listar(LocalDate fecha) { return recepciones.listar(fecha); }

    @Override
    public Recepcion obtener(long id) {
        return recepciones.buscar(id).orElseThrow(RecepcionChecks::missing);
    }
}
