package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.application.produccion.gateway.BitacoraProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.CatalogoProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.InventarioProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.LoteGateway;
import pe.edu.upeu.moliselva.domain.produccion.EstadoLote;
import pe.edu.upeu.moliselva.domain.produccion.Lote;

public class FinalizarLote implements FinalizarLoteUseCase {
    private final LoteGateway lotes;
    private final CatalogoProduccionGateway catalogo;
    private final InventarioProduccionGateway inventario;
    private final BitacoraProduccionGateway bitacora;
    public FinalizarLote(LoteGateway lotes, CatalogoProduccionGateway catalogo,
            InventarioProduccionGateway inventario, BitacoraProduccionGateway bitacora) {
        this.lotes=lotes; this.catalogo=catalogo; this.inventario=inventario; this.bitacora=bitacora;
    }
    @Override
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
