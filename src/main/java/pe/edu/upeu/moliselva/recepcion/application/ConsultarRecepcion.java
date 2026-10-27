package pe.edu.upeu.moliselva.recepcion.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.recepcion.application.port.in.ConsultarRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.out.RecepcionPort;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;

@Service
public class ConsultarRecepcion implements ConsultarRecepcionUseCase {
    private final RecepcionPort recepciones;
    public ConsultarRecepcion(RecepcionPort recepciones) { this.recepciones=recepciones; }

    @Override @Transactional(readOnly=true)
    public List<Recepcion> listar(LocalDate fecha) { return recepciones.listar(fecha); }

    @Override @Transactional(readOnly=true)
    public Recepcion obtener(long id) {
        return recepciones.buscar(id).orElseThrow(RecepcionChecks::missing);
    }
}
