package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.application.produccion.gateway.BitacoraProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.InventarioProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.LoteGateway;
import pe.edu.upeu.moliselva.domain.produccion.EstadoLote;
import pe.edu.upeu.moliselva.domain.produccion.Lote;

public class CambiarEstadoLote implements CambiarEstadoLoteUseCase {
    private final LoteGateway lotes;
    private final InventarioProduccionGateway inventario;
    private final BitacoraProduccionGateway bitacora;
    public CambiarEstadoLote(LoteGateway lotes, InventarioProduccionGateway inventario,
            BitacoraProduccionGateway bitacora) {
        this.lotes=lotes; this.inventario=inventario; this.bitacora=bitacora;
    }
    @Override
    public Lote cambiar(long id, EstadoLote estado, long usuarioId) {
        var lote=lotes.bloquear(id).orElseThrow(() -> LoteChecks.missing("LOTE"));
        if (estado==EstadoLote.EN_PROCESO) {
            if (lote.estado()!=EstadoLote.PLANIFICADO || !lotes.tieneInsumos(id)) {
                throw LoteChecks.locked("El lote debe estar planificado y tener insumos para iniciar");
            }
        } else if (estado==EstadoLote.ANULADO) {
            if ((lote.estado()!=EstadoLote.PLANIFICADO && lote.estado()!=EstadoLote.EN_PROCESO) ||
                    inventario.tieneMovimientos(id)) {
                throw LoteChecks.locked("El lote no se puede anular después de registrar movimientos");
            }
        } else {
            LoteChecks.require(false,"DATOS_INVALIDOS","Estado de lote no permitido");
        }
        var actualizado=lotes.cambiarEstado(id,estado,usuarioId);
        bitacora.registrar(usuarioId,"CAMBIAR_ESTADO_LOTE id="+id+"/"+estado);
        return actualizado;
    }
}
