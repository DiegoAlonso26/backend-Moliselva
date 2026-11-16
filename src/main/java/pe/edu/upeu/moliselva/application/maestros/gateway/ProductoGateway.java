package pe.edu.upeu.moliselva.application.maestros.gateway;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.maestros.Producto;
import pe.edu.upeu.moliselva.domain.maestros.ProductoDatos;

public interface ProductoGateway {
    List<Producto> activos();
    Optional<Producto> buscarActivo(long id);
    Producto crear(ProductoDatos datos);
    Optional<Producto> actualizar(long id, ProductoDatos datos);
    boolean desactivar(long id);
    /** Figura en una fórmula vigente, un lote activo, un pedido abierto o un despacho en borrador. */
    boolean enUso(long id);
}
