package pe.edu.upeu.moliselva.produccion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.produccion.application.port.in.FinalizarLoteUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.out.BitacoraProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.CatalogoProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.InventarioProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.LotePort;
import pe.edu.upeu.moliselva.produccion.domain.EstadoLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;

@Service
public class FinalizarLote implements FinalizarLoteUseCase {
    private final LotePort lotes;
    private final CatalogoProduccionPort catalogo;
    private final InventarioProduccionPort inventario;
    private final BitacoraProduccionPort bitacora;
    public FinalizarLote(LotePort lotes, CatalogoProduccionPort catalogo,
            InventarioProduccionPort inventario, BitacoraProduccionPort bitacora) {
        this.lotes=lotes; this.catalogo=catalogo; this.inventario=inventario; this.bitacora=bitacora;
    }
    @Override @Transactional
    public Lote finalizar(long id, long usuarioId) {
        var lote=lotes.bloquear(id).orElseThrow(() -> LoteChecks.missing("LOTE"));
        if (lote.estado()!=EstadoLote.EN_PROCESO) {
            throw LoteChecks.locked("Solo puede finalizar un lote en proceso");
        }
        var insumos=lotes.listarInsumos(id);
        if (insumos.isEmpty()) { throw LoteChecks.locked("El lote no tiene insumos"); }
        if (lotes.faltanInsumosFormula(id,lote.idFormula())) {
            throw LoteChecks.locked("Faltan insumos de la fórmula en el lote");
        }
        for (var insumo : insumos) {
            LoteChecks.insumoActivo(catalogo,insumo.idInsumo());
            inventario.registrarSalida(id,insumo.idInsumo(),insumo.cantidadConsumidaKg(),usuarioId);
        }
        inventario.registrarEntrada(id,lote.idProducto(),lote.tamanoKg(),usuarioId);
        lotes.marcarTerminado(id,usuarioId);
        bitacora.registrar(usuarioId,"FINALIZAR_LOTE id="+id);
        return lotes.buscar(id).orElseThrow(() -> LoteChecks.missing("LOTE"));
    }
}
