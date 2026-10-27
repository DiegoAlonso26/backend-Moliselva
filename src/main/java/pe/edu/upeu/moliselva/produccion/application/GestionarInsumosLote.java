package pe.edu.upeu.moliselva.produccion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.produccion.application.port.in.GestionarInsumosLoteUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.out.BitacoraProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.CatalogoProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.InventarioProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.LotePort;
import pe.edu.upeu.moliselva.produccion.domain.DatosInsumo;
import pe.edu.upeu.moliselva.produccion.domain.LoteInsumo;

@Service
public class GestionarInsumosLote implements GestionarInsumosLoteUseCase {
    private final LotePort lotes;
    private final CatalogoProduccionPort catalogo;
    private final InventarioProduccionPort inventario;
    private final BitacoraProduccionPort bitacora;
    public GestionarInsumosLote(LotePort lotes, CatalogoProduccionPort catalogo,
            InventarioProduccionPort inventario, BitacoraProduccionPort bitacora) {
        this.lotes=lotes; this.catalogo=catalogo; this.inventario=inventario; this.bitacora=bitacora;
    }
    @Override @Transactional
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
    @Override @Transactional
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
    @Override @Transactional
    public void eliminar(long loteId, long insumoId, long usuarioId) {
        var lote=lotes.bloquear(loteId).orElseThrow(() -> LoteChecks.missing("LOTE"));
        LoteChecks.planificado(lote,inventario);
        lotes.buscarInsumo(loteId,insumoId).orElseThrow(() -> LoteChecks.missing("LOTE_INSUMO"));
        lotes.eliminarInsumo(loteId,insumoId);
        bitacora.registrar(usuarioId,"ELIMINAR_LOTE_INSUMO id="+loteId+"/"+insumoId);
    }
}
