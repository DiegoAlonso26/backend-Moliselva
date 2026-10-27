package pe.edu.upeu.moliselva.ventas.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.ventas.application.port.in.GestionarDetallesDespachoUseCase;
import pe.edu.upeu.moliselva.ventas.application.port.out.BitacoraVentaPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.CatalogoVentaPort;
import pe.edu.upeu.moliselva.ventas.application.port.out.DespachoPort;
import pe.edu.upeu.moliselva.ventas.domain.DatosDetalle;
import pe.edu.upeu.moliselva.ventas.domain.DespachoDetalle;

@Service
public class GestionarDetallesDespacho implements GestionarDetallesDespachoUseCase {
    private final DespachoPort despachos;
    private final CatalogoVentaPort catalogo;
    private final BitacoraVentaPort bitacora;
    public GestionarDetallesDespacho(DespachoPort despachos, CatalogoVentaPort catalogo,
            BitacoraVentaPort bitacora) {
        this.despachos=despachos; this.catalogo=catalogo; this.bitacora=bitacora;
    }
    @Override @Transactional
    public DespachoDetalle agregar(long id, DatosDetalle datos, long usuarioId) {
        editable(id);
        VentaChecks.require(catalogo.productoActivo(datos.idProducto()),"REFERENCIA_INVALIDA",
            "El registro relacionado no existe o está inactivo");
        var detalle=despachos.agregarDetalle(id,datos);
        bitacora.registrar(usuarioId,"CREAR_DESPACHO_DETALLE id="+id+"/"+datos.idProducto());
        return detalle;
    }
    @Override @Transactional
    public DespachoDetalle actualizar(long id, long productoId, DatosDetalle datos, long usuarioId) {
        VentaChecks.require(datos.idProducto()==productoId,"DATOS_INVALIDOS",
            "El idProducto del cuerpo debe coincidir con la ruta");
        editable(id);
        despachos.buscarDetalle(id,productoId).orElseThrow(() -> VentaChecks.missing("DESPACHO_DETALLE"));
        var detalle=despachos.actualizarDetalle(id,productoId,datos);
        bitacora.registrar(usuarioId,"ACTUALIZAR_DESPACHO_DETALLE id="+id+"/"+productoId);
        return detalle;
    }
    @Override @Transactional
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
