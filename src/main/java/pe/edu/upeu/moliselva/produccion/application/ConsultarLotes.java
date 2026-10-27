package pe.edu.upeu.moliselva.produccion.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.produccion.application.port.in.ConsultarLotesUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.out.LotePort;
import pe.edu.upeu.moliselva.produccion.domain.Lote;
import pe.edu.upeu.moliselva.produccion.domain.LoteInsumo;

@Service
public class ConsultarLotes implements ConsultarLotesUseCase {
    private final LotePort lotes;
    public ConsultarLotes(LotePort lotes) { this.lotes = lotes; }
    @Override @Transactional(readOnly=true)
    public List<Lote> listar() { return lotes.listar(); }
    @Override @Transactional(readOnly=true)
    public Lote obtener(long id) { return lotes.buscar(id).orElseThrow(() -> LoteChecks.missing("LOTE")); }
    @Override @Transactional(readOnly=true)
    public List<LoteInsumo> listarInsumos(long id) {
        obtener(id);
        return lotes.listarInsumos(id);
    }
    @Override @Transactional(readOnly=true)
    public LoteInsumo obtenerInsumo(long id, long insumoId) {
        return lotes.buscarInsumo(id,insumoId)
            .orElseThrow(() -> LoteChecks.missing("LOTE_INSUMO"));
    }
}
