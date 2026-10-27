package pe.edu.upeu.moliselva.produccion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.produccion.application.port.in.CambiarEstadoLoteUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.out.BitacoraProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.InventarioProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.LotePort;
import pe.edu.upeu.moliselva.produccion.domain.EstadoLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;

@Service
public class CambiarEstadoLote implements CambiarEstadoLoteUseCase {
    private final LotePort lotes;
    private final InventarioProduccionPort inventario;
    private final BitacoraProduccionPort bitacora;
    public CambiarEstadoLote(LotePort lotes, InventarioProduccionPort inventario,
            BitacoraProduccionPort bitacora) {
        this.lotes=lotes; this.inventario=inventario; this.bitacora=bitacora;
    }
    @Override @Transactional
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
