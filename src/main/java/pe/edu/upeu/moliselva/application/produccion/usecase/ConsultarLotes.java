package pe.edu.upeu.moliselva.application.produccion.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.produccion.gateway.LoteGateway;
import pe.edu.upeu.moliselva.domain.produccion.Lote;
import pe.edu.upeu.moliselva.domain.produccion.LoteInsumo;

public class ConsultarLotes implements ConsultarLotesUseCase {
    private final LoteGateway lotes;
    public ConsultarLotes(LoteGateway lotes) { this.lotes = lotes; }
    @Override
    public List<Lote> listar() { return lotes.listar(); }
    @Override
    public Lote obtener(long id) { return lotes.buscar(id).orElseThrow(() -> LoteChecks.missing("LOTE")); }
    @Override
    public List<LoteInsumo> listarInsumos(long id) {
        obtener(id);
        return lotes.listarInsumos(id);
    }
    @Override
    public LoteInsumo obtenerInsumo(long id, long insumoId) {
        return lotes.buscarInsumo(id,insumoId)
            .orElseThrow(() -> LoteChecks.missing("LOTE_INSUMO"));
    }
}
