package pe.edu.upeu.moliselva.presentation.rest.ventas.mapper;

import java.util.LinkedHashMap;
import java.util.Map;
import pe.edu.upeu.moliselva.domain.ventas.Pedido;
import pe.edu.upeu.moliselva.domain.ventas.PedidoDetalle;
import pe.edu.upeu.moliselva.domain.ventas.Despacho;
import pe.edu.upeu.moliselva.domain.ventas.DespachoDetalle;

public final class VentaView {
    private VentaView() {}
    public static Map<String,Object> from(Pedido pedido) {
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("idPedido",pedido.idPedido());
        out.put("idCliente",pedido.idCliente());
        out.put("fechaHora",pedido.fechaHora());
        out.put("estado",pedido.estado().name());
        out.put("observacion",pedido.observacion());
        return out;
    }
    public static Map<String,Object> from(PedidoDetalle detalle) {
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("idPedido",detalle.idPedido());
        out.put("idProducto",detalle.idProducto());
        out.put("cantidadSacos",detalle.cantidadSacos());
        out.put("cantidadKg",detalle.cantidadKg());
        return out;
    }
    public static Map<String,Object> from(Despacho despacho) {
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("idDespacho",despacho.idDespacho());
        out.put("idPedido",despacho.idPedido());
        out.put("fechaHora",despacho.fechaHora());
        out.put("tipoEntrega",despacho.tipoEntrega().name());
        out.put("destino",despacho.destino());
        out.put("estado",despacho.estado().name());
        out.put("idUsuario",despacho.idUsuario());
        return out;
    }
    public static Map<String,Object> from(DespachoDetalle detalle) {
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("idDespacho",detalle.idDespacho());
        out.put("idProducto",detalle.idProducto());
        out.put("cantidadSacos",detalle.cantidadSacos());
        out.put("cantidadKg",detalle.cantidadKg());
        return out;
    }
}
