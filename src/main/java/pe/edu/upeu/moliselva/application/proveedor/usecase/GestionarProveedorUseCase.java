package pe.edu.upeu.moliselva.application.proveedor.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;
import pe.edu.upeu.moliselva.domain.proveedor.ProveedorDatos;

/** Alta, modificación y baja lógica de proveedores. Solo ADMINISTRADOR o JEFE_PLANTA. */
public interface GestionarProveedorUseCase {
    Proveedor crear(ProveedorDatos datos, Actor actor);
    Proveedor actualizar(long id, ProveedorDatos datos, Actor actor);
    void desactivar(long id, Actor actor);
}
