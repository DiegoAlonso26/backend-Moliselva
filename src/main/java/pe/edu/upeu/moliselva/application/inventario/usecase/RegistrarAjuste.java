package pe.edu.upeu.moliselva.application.inventario.usecase;

import pe.edu.upeu.moliselva.application.inventario.gateway.InventarioGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.inventario.AjusteInventario;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoInventario;

public class RegistrarAjuste implements RegistrarAjusteUseCase {
    private final InventarioGateway inventario;
    private final BitacoraGateway bitacora;

    public RegistrarAjuste(InventarioGateway inventario, BitacoraGateway bitacora) {
        this.inventario=inventario; this.bitacora=bitacora;
    }

    @Override
    public MovimientoInventario ajustar(AjusteInventario ajuste, long usuarioId) {
        if (!ajuste.itemValido()) {
            throw FallaAplicacion.datosInvalidos("ITEM_INVALIDO","Envíe un idInsumo o un idProducto válido, pero no ambos");
        }
        if (!inventario.itemActivo(ajuste.idInsumo(),ajuste.idProducto())) {
            throw FallaAplicacion.datosInvalidos("ITEM_INVALIDO","El insumo o producto no existe o está inactivo");
        }
        long id = inventario.insertarAjuste(ajuste,inventario.unidadMedida(ajuste.idInsumo()),usuarioId);
        bitacora.registrar(usuarioId,"AJUSTE_INVENTARIO id="+id,ajuste.motivo().trim());
        return inventario.movimiento(id).orElseThrow(() ->
            FallaAplicacion.noEncontrado("MOVIMIENTO_NO_ENCONTRADO","El movimiento no existe"));
    }
}
