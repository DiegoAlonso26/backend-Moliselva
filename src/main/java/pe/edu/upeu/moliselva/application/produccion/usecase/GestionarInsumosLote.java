package pe.edu.upeu.moliselva.application.produccion.usecase;

import pe.edu.upeu.moliselva.application.produccion.gateway.BitacoraProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.CatalogoProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.InventarioProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.LoteGateway;
import pe.edu.upeu.moliselva.domain.produccion.DatosInsumo;
import pe.edu.upeu.moliselva.domain.produccion.LoteInsumo;

public class GestionarInsumosLote implements GestionarInsumosLoteUseCase {
    private final LoteGateway lotes;
    private final CatalogoProduccionGateway catalogo;
    private final InventarioProduccionGateway inventario;
    private final BitacoraProduccionGateway bitacora;
    public GestionarInsumosLote(LoteGateway lotes, CatalogoProduccionGateway catalogo,
            InventarioProduccionGateway inventario, BitacoraProduccionGateway bitacora) {
        this.lotes=lotes; this.catalogo=catalogo; this.inventario=inventario; this.bitacora=bitacora;
    }
    @Override
    public LoteInsumo agregar(long loteId, DatosInsumo datos, long usuarioId) {
        var lote=lotes.bloquear(loteId).orElseThrow(() -> LoteChecks.missing("LOTE"));
        LoteChecks.planificado(lote,inventario);
        LoteChecks.insumoActivo(catalogo,datos.idInsumo());
        LoteChecks.require(catalogo.insumoEnFormula(loteId,datos.idInsumo()),
            "INSUMO_FUERA_DE_FORMULA","El insumo no pertenece a la fórmula del lote");
        var insumo=lotes.agregarInsumo(loteId,datos);
        bitacora.registrar(usuarioId,"CREAR_LOTE_INSUMO id="+loteId+"/"+datos.idInsumo());
        return insumo;
    }
    @Override
    public LoteInsumo actualizar(long loteId, long insumoId, DatosInsumo datos, long usuarioId) {
        LoteChecks.require(datos.idInsumo()==insumoId,"DATOS_INVALIDOS",
            "El idInsumo del cuerpo debe coincidir con la ruta");
        var lote=lotes.bloquear(loteId).orElseThrow(() -> LoteChecks.missing("LOTE"));
        LoteChecks.insumoCorregible(lote,inventario);
        lotes.buscarInsumo(loteId,insumoId).orElseThrow(() -> LoteChecks.missing("LOTE_INSUMO"));
        var insumo=lotes.actualizarInsumo(loteId,insumoId,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_LOTE_INSUMO id="+loteId+"/"+insumoId);
        return insumo;
    }
    @Override
    public void eliminar(long loteId, long insumoId, long usuarioId) {
        var lote=lotes.bloquear(loteId).orElseThrow(() -> LoteChecks.missing("LOTE"));
        LoteChecks.planificado(lote,inventario);
        lotes.buscarInsumo(loteId,insumoId).orElseThrow(() -> LoteChecks.missing("LOTE_INSUMO"));
        lotes.eliminarInsumo(loteId,insumoId);
        bitacora.registrar(usuarioId,"ELIMINAR_LOTE_INSUMO id="+loteId+"/"+insumoId);
    }
}
