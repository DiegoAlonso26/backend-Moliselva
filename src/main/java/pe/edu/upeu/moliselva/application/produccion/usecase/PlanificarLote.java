package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.application.produccion.gateway.BitacoraProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.CatalogoProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.InventarioProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.LoteGateway;
import pe.edu.upeu.moliselva.domain.produccion.DatosLote;
import pe.edu.upeu.moliselva.domain.produccion.Lote;

public class PlanificarLote implements PlanificarLoteUseCase {
    private final LoteGateway lotes;
    private final CatalogoProduccionGateway catalogo;
    private final InventarioProduccionGateway inventario;
    private final BitacoraProduccionGateway bitacora;
    public PlanificarLote(LoteGateway lotes, CatalogoProduccionGateway catalogo,
            InventarioProduccionGateway inventario, BitacoraProduccionGateway bitacora) {
        this.lotes=lotes; this.catalogo=catalogo; this.inventario=inventario; this.bitacora=bitacora;
    }
    @Override
    public Lote crear(DatosLote datos, long usuarioId) {
        LoteChecks.datos(datos,catalogo);
        Lote lote=lotes.crear(datos,usuarioId);
        bitacora.registrar(usuarioId,"CREAR_LOTE id="+lote.idLote());
        return lote;
    }
    @Override
    public Lote actualizar(long id, DatosLote datos, long usuarioId) {
        var actual=lotes.bloquear(id).orElseThrow(() -> LoteChecks.missing("LOTE"));
        LoteChecks.planificado(actual,inventario);
        LoteChecks.datos(datos,catalogo);
        var lote=lotes.actualizar(id,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_LOTE id="+id);
        return lote;
    }
    @Override
    public void eliminar(long id, long usuarioId) {
        var actual=lotes.bloquear(id).orElseThrow(() -> LoteChecks.missing("LOTE"));
        LoteChecks.planificado(actual,inventario);
        lotes.eliminar(id);
        bitacora.registrar(usuarioId,"ELIMINAR_LOTE id="+id);
    }
}
