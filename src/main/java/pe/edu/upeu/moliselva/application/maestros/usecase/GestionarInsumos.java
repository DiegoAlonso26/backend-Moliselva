package pe.edu.upeu.moliselva.application.maestros.usecase;

import pe.edu.upeu.moliselva.application.maestros.gateway.InsumoGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.StockItemGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.maestros.Insumo;
import pe.edu.upeu.moliselva.domain.maestros.InsumoDatos;
import pe.edu.upeu.moliselva.domain.maestros.Maestros;

public class GestionarInsumos implements GestionarInsumosUseCase {
    private final InsumoGateway insumos;
    private final StockItemGateway stock;
    private final BitacoraGateway bitacora;

    public GestionarInsumos(InsumoGateway insumos, StockItemGateway stock, BitacoraGateway bitacora) {
        this.insumos=insumos; this.stock=stock; this.bitacora=bitacora;
    }

    @Override
    public Insumo crear(InsumoDatos datos, long usuarioId) {
        var insumo=insumos.crear(datos);
        bitacora.registrar(usuarioId,"CREAR_INSUMO id="+insumo.idInsumo(),null);
        return insumo;
    }

    @Override
    public Insumo actualizar(long id, InsumoDatos datos, long usuarioId) {
        protegerInsumoSistema(id);
        var insumo=insumos.actualizar(id,datos).orElseThrow(() -> MaestrosFallas.noEncontrado("INSUMO"));
        bitacora.registrar(usuarioId,"ACTUALIZAR_INSUMO id="+id,null);
        return insumo;
    }

    @Override
    public void desactivar(long id, long usuarioId) {
        protegerInsumoSistema(id);
        MaestrosFallas.sinStock(stock,"INSUMO",id);
        if (insumos.enUso(id)) {
            throw FallaAplicacion.conflicto("INSUMO_EN_USO","El insumo figura en una fórmula vigente o un lote activo");
        }
        if (!insumos.desactivar(id)) { throw MaestrosFallas.noEncontrado("INSUMO"); }
        bitacora.registrar(usuarioId,"DESACTIVAR_INSUMO id="+id,null);
    }

    private void protegerInsumoSistema(long id) {
        if (insumos.buscarActivo(id).map(Maestros::esInsumoSistema).orElse(false)) {
            throw FallaAplicacion.conflicto("INSUMO_SISTEMA",
                "El insumo Maíz sostiene el kardex de la demo y no se puede modificar ni desactivar");
        }
    }
}
