package pe.edu.upeu.moliselva.application.maestros.usecase;

import pe.edu.upeu.moliselva.application.maestros.gateway.ProductoGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.StockItemGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.maestros.Producto;
import pe.edu.upeu.moliselva.domain.maestros.ProductoDatos;

public class GestionarProductos implements GestionarProductosUseCase {
    private final ProductoGateway productos;
    private final StockItemGateway stock;
    private final BitacoraGateway bitacora;

    public GestionarProductos(ProductoGateway productos, StockItemGateway stock, BitacoraGateway bitacora) {
        this.productos=productos; this.stock=stock; this.bitacora=bitacora;
    }

    @Override
    public Producto crear(ProductoDatos datos, long usuarioId) {
        var producto=productos.crear(datos);
        bitacora.registrar(usuarioId,"CREAR_PRODUCTO id="+producto.idProducto(),null);
        return producto;
    }

    @Override
    public Producto actualizar(long id, ProductoDatos datos, long usuarioId) {
        var producto=productos.actualizar(id,datos).orElseThrow(() -> MaestrosFallas.noEncontrado("PRODUCTO"));
        bitacora.registrar(usuarioId,"ACTUALIZAR_PRODUCTO id="+id,null);
        return producto;
    }

    @Override
    public void desactivar(long id, long usuarioId) {
        MaestrosFallas.sinStock(stock,"PRODUCTO",id);
        if (productos.enUso(id)) {
            throw FallaAplicacion.conflicto("PRODUCTO_EN_USO",
                "El producto figura en una fórmula, lote, pedido o despacho activo");
        }
        if (!productos.desactivar(id)) { throw MaestrosFallas.noEncontrado("PRODUCTO"); }
        bitacora.registrar(usuarioId,"DESACTIVAR_PRODUCTO id="+id,null);
    }
}
