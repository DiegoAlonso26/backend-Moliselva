package pe.edu.upeu.moliselva.application.proveedor.gateway;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;
import pe.edu.upeu.moliselva.domain.proveedor.ProveedorDatos;

public interface ProveedorGateway {
    List<Proveedor> activos();
    Optional<Proveedor> buscarActivo(long id);
    Proveedor crear(ProveedorDatos datos);
    Optional<Proveedor> actualizar(long id, ProveedorDatos datos);
    boolean desactivar(long id);
}
