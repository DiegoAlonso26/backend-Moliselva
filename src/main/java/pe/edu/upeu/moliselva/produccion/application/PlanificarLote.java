package pe.edu.upeu.moliselva.produccion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.produccion.application.port.in.PlanificarLoteUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.out.BitacoraProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.CatalogoProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.InventarioProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.LotePort;
import pe.edu.upeu.moliselva.produccion.domain.DatosLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;

@Service
public class PlanificarLote implements PlanificarLoteUseCase {
    private final LotePort lotes;
    private final CatalogoProduccionPort catalogo;
    private final InventarioProduccionPort inventario;
    private final BitacoraProduccionPort bitacora;
    public PlanificarLote(LotePort lotes, CatalogoProduccionPort catalogo,
            InventarioProduccionPort inventario, BitacoraProduccionPort bitacora) {
        this.lotes=lotes; this.catalogo=catalogo; this.inventario=inventario; this.bitacora=bitacora;
    }
    @Override @Transactional
    public Lote crear(DatosLote datos, long usuarioId) {
        LoteChecks.datos(datos,catalogo);
        Lote lote=lotes.crear(datos,usuarioId);
        bitacora.registrar(usuarioId,"CREAR_LOTE id="+lote.idLote());
        return lote;
    }
    @Override @Transactional
    public Lote actualizar(long id, DatosLote datos, long usuarioId) {
        var actual=lotes.bloquear(id).orElseThrow(() -> LoteChecks.missing("LOTE"));
        LoteChecks.planificado(actual,inventario);
        LoteChecks.datos(datos,catalogo);
        var lote=lotes.actualizar(id,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_LOTE id="+id);
        return lote;
    }
    @Override @Transactional
    public void eliminar(long id, long usuarioId) {
        var actual=lotes.bloquear(id).orElseThrow(() -> LoteChecks.missing("LOTE"));
        LoteChecks.planificado(actual,inventario);
        lotes.eliminar(id);
        bitacora.registrar(usuarioId,"ELIMINAR_LOTE id="+id);
    }
}
