package pe.edu.upeu.moliselva.application.ventas.usecase;

import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.CatalogoVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.DespachoGateway;
import pe.edu.upeu.moliselva.domain.ventas.DatosDetalle;
import pe.edu.upeu.moliselva.domain.ventas.DespachoDetalle;

public class GestionarDetallesDespacho implements GestionarDetallesDespachoUseCase {
    private final DespachoGateway despachos;
    private final CatalogoVentaGateway catalogo;
    private final BitacoraVentaGateway bitacora;
    public GestionarDetallesDespacho(DespachoGateway despachos, CatalogoVentaGateway catalogo,
            BitacoraVentaGateway bitacora) {
        this.despachos=despachos; this.catalogo=catalogo; this.bitacora=bitacora;
    }
    @Override
    public DespachoDetalle agregar(long id, DatosDetalle datos, long usuarioId) {
        editable(id);
        VentaChecks.require(catalogo.productoActivo(datos.idProducto()),"REFERENCIA_INVALIDA",
            "El registro relacionado no existe o está inactivo");
        var detalle=despachos.agregarDetalle(id,datos);
        bitacora.registrar(usuarioId,"CREAR_DESPACHO_DETALLE id="+id+"/"+datos.idProducto());
        return detalle;
    }
    @Override
    public DespachoDetalle actualizar(long id, long productoId, DatosDetalle datos, long usuarioId) {
        VentaChecks.require(datos.idProducto()==productoId,"DATOS_INVALIDOS",
            "El idProducto del cuerpo debe coincidir con la ruta");
        editable(id);
        despachos.buscarDetalle(id,productoId).orElseThrow(() -> VentaChecks.missing("DESPACHO_DETALLE"));
        var detalle=despachos.actualizarDetalle(id,productoId,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_DESPACHO_DETALLE id="+id+"/"+productoId);
        return detalle;
    }
    @Override
    public void eliminar(long id, long productoId, long usuarioId) {
        editable(id);
        despachos.buscarDetalle(id,productoId).orElseThrow(() -> VentaChecks.missing("DESPACHO_DETALLE"));
        despachos.eliminarDetalle(id,productoId);
        bitacora.registrar(usuarioId,"ELIMINAR_DESPACHO_DETALLE id="+id+"/"+productoId);
    }
    private void editable(long id) {
        VentaChecks.borrador(despachos.bloquear(id).orElseThrow(() -> VentaChecks.missing("DESPACHO")));
    }
}
