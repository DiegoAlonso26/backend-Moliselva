package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.DespachoGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.PedidoGateway;
import pe.edu.upeu.moliselva.domain.ventas.DatosDespacho;
import pe.edu.upeu.moliselva.domain.ventas.Despacho;

public class GestionarDespachos implements GestionarDespachosUseCase {
    private final DespachoGateway despachos;
    private final PedidoGateway pedidos;
    private final BitacoraVentaGateway bitacora;
    public GestionarDespachos(DespachoGateway despachos, PedidoGateway pedidos,
            BitacoraVentaGateway bitacora) {
        this.despachos=despachos; this.pedidos=pedidos; this.bitacora=bitacora;
    }
    @Override
    public Despacho crear(DatosDespacho datos, long usuarioId) {
        var pedido=pedidos.bloquear(datos.idPedido()).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        VentaChecks.datosDespacho(datos,pedido);
        var despacho=despachos.crear(datos,usuarioId);
        bitacora.registrar(usuarioId,"CREAR_DESPACHO id="+despacho.idDespacho());
        return despacho;
    }
    @Override
    public Despacho actualizar(long id, DatosDespacho datos, long usuarioId) {
        var despacho=despachos.bloquear(id).orElseThrow(() -> VentaChecks.missing("DESPACHO"));
        VentaChecks.borrador(despacho);
        var pedido=pedidos.bloquear(datos.idPedido()).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        VentaChecks.datosDespacho(datos,pedido);
        var actualizado=despachos.actualizar(id,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_DESPACHO id="+id);
        return actualizado;
    }
    @Override
    public void eliminar(long id, long usuarioId) {
        var despacho=despachos.bloquear(id).orElseThrow(() -> VentaChecks.missing("DESPACHO"));
        VentaChecks.borrador(despacho);
        if (despachos.tieneMovimientos(id)) {
            throw VentaChecks.locked("El despacho ya tiene movimientos de inventario");
        }
        despachos.eliminar(id);
        bitacora.registrar(usuarioId,"ELIMINAR_DESPACHO id="+id);
    }
}
