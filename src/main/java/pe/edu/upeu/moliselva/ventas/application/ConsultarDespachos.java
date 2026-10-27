package pe.edu.upeu.moliselva.ventas.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.ventas.application.port.in.ConsultarDespachosUseCase;
import pe.edu.upeu.moliselva.ventas.application.port.out.DespachoPort;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;
import pe.edu.upeu.moliselva.ventas.domain.DespachoDetalle;

@Service
public class ConsultarDespachos implements ConsultarDespachosUseCase {
    private final DespachoPort despachos;
    public ConsultarDespachos(DespachoPort despachos) { this.despachos=despachos; }
    @Override @Transactional(readOnly=true)
    public List<Despacho> listar() { return despachos.listar(); }
    @Override @Transactional(readOnly=true)
    public Despacho obtener(long id) {
        return despachos.buscar(id).orElseThrow(() -> VentaChecks.missing("DESPACHO"));
    }
    @Override @Transactional(readOnly=true)
    public List<DespachoDetalle> listarDetalles(long id) {
        obtener(id);
        return despachos.listarDetalles(id);
    }
    @Override @Transactional(readOnly=true)
    public DespachoDetalle obtenerDetalle(long id, long productoId) {
        return despachos.buscarDetalle(id,productoId)
            .orElseThrow(() -> VentaChecks.missing("DESPACHO_DETALLE"));
    }
}
