package pe.edu.upeu.moliselva.application.ventas.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.ventas.gateway.DespachoGateway;
import pe.edu.upeu.moliselva.domain.ventas.Despacho;
import pe.edu.upeu.moliselva.domain.ventas.DespachoDetalle;

public class ConsultarDespachos implements ConsultarDespachosUseCase {
    private final DespachoGateway despachos;
    public ConsultarDespachos(DespachoGateway despachos) { this.despachos=despachos; }
    @Override
    public List<Despacho> listar() { return despachos.listar(); }
    @Override
    public Despacho obtener(long id) {
        return despachos.buscar(id).orElseThrow(() -> VentaChecks.missing("DESPACHO"));
    }
    @Override
    public List<DespachoDetalle> listarDetalles(long id) {
        obtener(id);
        return despachos.listarDetalles(id);
    }
    @Override
    public DespachoDetalle obtenerDetalle(long id, long productoId) {
        return despachos.buscarDetalle(id,productoId)
            .orElseThrow(() -> VentaChecks.missing("DESPACHO_DETALLE"));
    }
}
