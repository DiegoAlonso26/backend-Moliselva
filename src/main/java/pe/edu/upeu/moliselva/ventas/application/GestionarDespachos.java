package pe.edu.upeu.moliselva.ventas.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.ventas.application.port.in.GestionarDespachosUseCase;
import pe.edu.upeu.moliselva.ventas.application.port.out.BitacoraVentaPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.DespachoPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.PedidoPort;
import pe.edu.upeu.moliselva.ventas.domain.DatosDespacho;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;

@Service
public class GestionarDespachos implements GestionarDespachosUseCase {
    private final DespachoPort despachos;
    private final PedidoPort pedidos;
    private final BitacoraVentaPort bitacora;
    public GestionarDespachos(DespachoPort despachos, PedidoPort pedidos,
            BitacoraVentaPort bitacora) {
        this.despachos=despachos; this.pedidos=pedidos; this.bitacora=bitacora;
    }
    @Override @Transactional
    public Despacho crear(DatosDespacho datos, long usuarioId) {
        var pedido=pedidos.bloquear(datos.idPedido()).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        VentaChecks.datosDespacho(datos,pedido);
        var despacho=despachos.crear(datos,usuarioId);
        bitacora.registrar(usuarioId,"CREAR_DESPACHO id="+despacho.idDespacho());
        return despacho;
    }
    @Override @Transactional
    public Despacho actualizar(long id, DatosDespacho datos, long usuarioId) {
        var despacho=despachos.bloquear(id).orElseThrow(() -> VentaChecks.missing("DESPACHO"));
        VentaChecks.borrador(despacho);
        var pedido=pedidos.bloquear(datos.idPedido()).orElseThrow(() -> VentaChecks.missing("PEDIDO"));
        VentaChecks.datosDespacho(datos,pedido);
        var actualizado=despachos.actualizar(id,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_DESPACHO id="+id);
        return actualizado;
    }
    @Override @Transactional
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
