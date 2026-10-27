package pe.edu.upeu.moliselva.ventas.application;

import pe.edu.upeu.moliselva.ventas.domain.DatosDespacho;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;
import pe.edu.upeu.moliselva.ventas.domain.DespachoPolicy;
import pe.edu.upeu.moliselva.ventas.domain.EstadoDespacho;
import pe.edu.upeu.moliselva.ventas.domain.EstadoPedido;
import pe.edu.upeu.moliselva.ventas.domain.Pedido;

final class VentaChecks {
    private VentaChecks() {}
    static VentaFailure missing(String type) {
        return new VentaFailure(VentaFailure.Type.NO_ENCONTRADO,
            type+"_NO_ENCONTRADO","El registro no existe");
    }
    static VentaFailure locked(String message) {
        return new VentaFailure(VentaFailure.Type.CONFLICTO,"REGISTRO_NO_EDITABLE",message);
    }
    static void require(boolean ok, String code, String message) {
        if (!ok) { throw new VentaFailure(VentaFailure.Type.DATOS_INVALIDOS,code,message); }
    }
    static void pendiente(Pedido pedido) {
        if (pedido.estado()!=EstadoPedido.PENDIENTE) {
            throw locked("Solo puede modificar un pedido pendiente");
        }
    }
    static void borrador(Despacho despacho) {
        if (despacho.estado()!=EstadoDespacho.BORRADOR) {
            throw locked("Solo puede modificar un despacho en borrador");
        }
    }
    static void datosDespacho(DatosDespacho datos, Pedido pedido) {
        require(DespachoPolicy.pedidoAdmiteDespacho(pedido),"PEDIDO_INVALIDO",
            "El pedido no admite nuevos despachos");
        require(!DespachoPolicy.requiereDestino(datos),"DESTINO_OBLIGATORIO",
            "El reparto requiere destino");
    }
}
